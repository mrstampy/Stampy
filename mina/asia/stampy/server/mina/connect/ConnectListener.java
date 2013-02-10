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
package asia.stampy.server.mina.connect;

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

/**
 *
 */
@Resource
public class ConnectListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Queue<HostPort> connectedClients = new ConcurrentLinkedQueue<>();
	private ServerMinaMessageGateway gateway;

	private static StompMessageType[] TYPES = StompMessageType.values();

	/*
	 * (non-Javadoc)
	 * 
	 * @see asia.stampy.common.mina.StampyMinaMessageListener#getMessageTypes()
	 */
	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.StampyMinaMessageListener#isForMessage(asia.stampy
	 * .common.message.StampyMessage)
	 */
	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.StampyMinaMessageListener#messageReceived(asia.
	 * stampy.common.message.StampyMessage,
	 * org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
	 */
	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		switch (message.getMessageType()) {
		case ABORT:
		case ACK:
		case BEGIN:
		case COMMIT:
		case NACK:
		case SEND:
		case SUBSCRIBE:
		case UNSUBSCRIBE:
			checkConnected(hostPort);
			break;
		case CONNECT:
		case STOMP:
			checkDisconnected(hostPort);
			connectedClients.add(hostPort);
			break;
		case DISCONNECT:
			connectedClients.remove(hostPort);
			break;
		default:
			throw new IllegalArgumentException("Unexpected message type " + message.getMessageType());

		}

	}

	private void checkDisconnected(HostPort hostPort) throws AlreadyConnectedException {
		if (!connectedClients.contains(hostPort)) return;

		throw new AlreadyConnectedException(hostPort + " is already connected");
	}

	private void checkConnected(HostPort hostPort) throws NotConnectedException {
		if (connectedClients.contains(hostPort)) return;

		throw new NotConnectedException("CONNECT message required for " + hostPort);
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
	 * @param gateway
	 *          the new gateway
	 */
	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;

		gateway.addServiceListener(new MinaServiceAdapter() {

			public void sessionDestroyed(IoSession session) throws Exception {
				HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
				if (connectedClients.contains(hostPort)) {
					log.debug("{} session terminated with outstanding connection, cleaning up", hostPort);
					connectedClients.remove(hostPort);
				}
			}
		});
	}

}
