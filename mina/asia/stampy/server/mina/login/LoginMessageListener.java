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
package asia.stampy.server.mina.login;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.mina.ServerMinaMessageGateway;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving loginMessage events.
 * The class that is interested in processing a loginMessage
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addLoginMessageListener<code> method. When
 * the loginMessage event occurs, that object's appropriate
 * method is invoked.
 *
 * @see LoginMessageEvent
 */
@Resource
public class LoginMessageListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static StompMessageType[] TYPES = StompMessageType.values();

	private Queue<HostPort> loggedInConnections = new ConcurrentLinkedQueue<>();

	private StampyLoginHandler loginHandler;
	private ServerMinaMessageGateway gateway;

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
		case ACK:
		case BEGIN:
		case COMMIT:
		case NACK:
		case SEND:
		case SUBSCRIBE:
		case UNSUBSCRIBE:
			loggedInCheck(message, hostPort);
			break;
		case CONNECT:
			logIn(session, hostPort, ((ConnectMessage) message).getHeader());
			break;
		case STOMP:
			logIn(session, hostPort, ((StompMessage) message).getHeader());
			break;
		case DISCONNECT:
			loggedInConnections.remove(hostPort);
			break;
		default:
			String error = "Unexpected message type " + message.getMessageType();
			log.error(error);
			throw new IllegalArgumentException(error);

		}
	}

	private void loggedInCheck(StampyMessage<?> message, HostPort hostPort) throws NotLoggedInException {
		if (loggedInConnections.contains(hostPort)) return;

		log.error("{} attempted to send a {} message without logging in", hostPort, message.getMessageType());
		throw new NotLoggedInException("Not logged in");
	}

	private void logIn(IoSession session, HostPort hostPort, ConnectHeader header) throws AlreadyLoggedInException,
			NotLoggedInException {
		if (loggedInConnections.contains(hostPort)) throw new AlreadyLoggedInException(hostPort + " is already logged in");

		if (!isForHeader(header)) throw new NotLoggedInException("login and passcode not specified, cannot log in");

		try {
			getLoginHandler().login(header.getLogin(), header.getPasscode());
			loggedInConnections.add(hostPort);
		} catch (TerminateSessionException e) {
			log.error(e.getMessage(), e);
			sendErrorMessage(e.getMessage(), hostPort);
			session.close(false);
		}
	}

	private void sendErrorMessage(String message, HostPort hostPort) {
		ErrorMessage error = new ErrorMessage("n/a");
		error.getHeader().setMessageHeader(message);

		try {
			getGateway().sendMessage(error, hostPort);
		} catch (InterceptException e) {
			log.error("Sending of login error message failed", e);
		}
	}

	private boolean isForHeader(ConnectHeader header) {
		return StringUtils.isNotEmpty(header.getLogin()) && StringUtils.isNotEmpty(header.getPasscode());
	}

	/**
	 * Gets the login handler.
	 *
	 * @return the login handler
	 */
	public StampyLoginHandler getLoginHandler() {
		return loginHandler;
	}

	/**
	 * Sets the login handler.
	 *
	 * @param loginHandler the new login handler
	 */
	public void setLoginHandler(StampyLoginHandler loginHandler) {
		this.loginHandler = loginHandler;
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
				if (loggedInConnections.contains(hostPort)) {
					log.debug("{} session terminated before DISCONNECT message received, cleaning up", hostPort);
					loggedInConnections.remove(hostPort);
				}
			}
		});
	}

}
