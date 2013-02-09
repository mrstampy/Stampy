/*
 * Copyright (C) 2013 Burton Alexander
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 */
package asia.stampy.server.mina.transaction;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving transaction events.
 * The class that is interested in processing a transaction
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTransactionListener<code> method. When
 * the transaction event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TransactionEvent
 */
@Resource
public class TransactionListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Queue<HostPort> activeTransactions = new ConcurrentLinkedQueue<>();
	private ServerMinaMessageGateway gateway;

	private static StompMessageType[] TYPES = { StompMessageType.ABORT, StompMessageType.BEGIN, StompMessageType.COMMIT,
			StompMessageType.DISCONNECT };

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaMessageListener#getMessageTypes()
	 */
	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaMessageListener#isForMessage(asia.stampy.common.message.StampyMessage)
	 */
	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaMessageListener#messageReceived(asia.stampy.common.message.StampyMessage, org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
	 */
	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		switch (message.getMessageType()) {
		case ABORT:
			abort(hostPort);
			break;
		case BEGIN:
			begin(hostPort);
			break;
		case COMMIT:
			commit(hostPort);
			break;
		case DISCONNECT:
			activeTransactions.remove(hostPort);
			break;
		default:
			break;

		}

	}

	private void commit(HostPort hostPort) throws TransactionNotStartedException {
		removeActiveTransaction(hostPort, "committed");
	}

	private void abort(HostPort hostPort) throws TransactionNotStartedException {
		removeActiveTransaction(hostPort, "aborted");
	}

	private void begin(HostPort hostPort) throws TransactionAlreadyStartedException {
		if (isNoTransaction(hostPort)) {
			log.info("Starting transaction for {}", hostPort);
			activeTransactions.add(hostPort);
		}
	}

	private boolean isNoTransaction(HostPort hostPort) throws TransactionAlreadyStartedException {
		if (activeTransactions.contains(hostPort)) {
			String error = "Transaction already started";
			throw new TransactionAlreadyStartedException(error);
		}

		return true;
	}

	private void removeActiveTransaction(HostPort hostPort, String function) throws TransactionNotStartedException {
		if (isTransactionStarted(hostPort)) {
			log.info("Transaction for {} {}", hostPort, function);
			activeTransactions.remove(hostPort);
		}
	}

	private boolean isTransactionStarted(HostPort hostPort) throws TransactionNotStartedException {
		if (!activeTransactions.contains(hostPort)) {
			String error = "Transaction not started";
			log.error(error);
			throw new TransactionNotStartedException(error);
		}

		return true;
	}

	/**
	 * Gets the gateway.
	 *
	 * @return the gateway
	 */
	public ServerMinaMessageGateway getGateway() {
		return gateway;
	}

	/**
	 * Sets the gateway.
	 *
	 * @param gateway the new gateway
	 */
	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;
		
		gateway.addServiceListener(new MinaServiceAdapter() {

			public void sessionDestroyed(IoSession session) throws Exception {
				HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
				if (activeTransactions.contains(hostPort)) {
					log.debug("{} session terminated with outstanding transaction, cleaning up", hostPort);
					activeTransactions.remove(hostPort);
				}
			}
		});
	}

}
