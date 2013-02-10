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
package asia.stampy.common;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import asia.stampy.common.message.interceptor.StampyOutgoingTextInterceptor;

/**
 * A StampyMessageGateway is the interface between the technology used to
 * connect to a STOMP implementation and the Stampy library. It is the class
 * through which STOMP messages are sent and received.<br>
 * <br>
 * Subclasses are singletons; wire into the system appropriately.
 */
public abstract class AbstractStampyMessageGateway {
	private Queue<StampyOutgoingMessageInterceptor> interceptors = new ConcurrentLinkedQueue<>();
	private Queue<StampyOutgoingTextInterceptor> textInterceptors = new ConcurrentLinkedQueue<>();

	private boolean autoShutdown;
	
	private int heartbeat;

	/**
	 * Broadcasts a {@link StampyMessage} to all connected clients from the server
	 * or to the server from a client. Use this method for all STOMP messages.
	 * 
	 * @param message
	 *          the message
	 */
	public void broadcastMessage(StampyMessage<?> message) throws InterceptException {
		interceptOutgoingMessage(message);
		broadcastMessage(message.toStompMessage(true));
	}

	/**
	 * Adds the specified outgoing message interceptor
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void addOutgoingMessageInterceptor(StampyOutgoingMessageInterceptor interceptor) {
		interceptors.add(interceptor);
	}

	/**
	 * Removes the specified outgoing message interceptor
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void removeOutgoingMessageInterceptor(StampyOutgoingMessageInterceptor interceptor) {
		interceptors.remove(interceptor);
	}

	/**
	 * Adds the specified outgoing message interceptors. For use by DI frameworks.
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void setOutgoingMessageInterceptors(Collection<StampyOutgoingMessageInterceptor> interceptors) {
		this.interceptors.addAll(interceptors);
	}

	/**
	 * Adds the specified outgoing message interceptor
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void addOutgoingTextInterceptor(StampyOutgoingTextInterceptor interceptor) {
		textInterceptors.add(interceptor);
	}

	/**
	 * Removes the specified outgoing message interceptor
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void removeOutgoingTextInterceptor(StampyOutgoingTextInterceptor interceptor) {
		textInterceptors.remove(interceptor);
	}

	/**
	 * Adds the specified outgoing message interceptors. For use by DI frameworks.
	 * 
	 * @param interceptor
	 * @see StampyOutgoingMessageInterceptor
	 */
	public void setOutgoingTextInterceptors(Collection<StampyOutgoingTextInterceptor> interceptors) {
		this.textInterceptors.addAll(interceptors);
	}

	protected void interceptOutgoingMessage(StampyMessage<?> message) throws InterceptException {
		for (StampyOutgoingMessageInterceptor interceptor : interceptors) {
			if (isForType(interceptor.getMessageTypes(), message.getMessageType()) && interceptor.isForMessage(message)) {
				interceptor.interceptMessage(message);
			}
		}
	}

	protected void interceptOutgoingMessage(String message) throws InterceptException {
		for (StampyOutgoingTextInterceptor interceptor : textInterceptors) {
			interceptor.interceptMessage(message);
		}
	}

	private boolean isForType(StompMessageType[] messageTypes, StompMessageType messageType) {
		for (StompMessageType type : messageTypes) {
			if (type.equals(messageType)) return true;
		}

		return false;
	}

	/**
	 * Broadcasts the specified String to all connections. Included for STOMP
	 * implementations which accept custom message types. Use for all non-STOMP
	 * messages.
	 * 
	 * @param stompMessage
	 *          the stomp message
	 */
	public abstract void broadcastMessage(String stompMessage) throws InterceptException;

	/**
	 * Sends the specified String to the specified {@link HostPort}. Included for
	 * STOMP implementations which accept custom message types. Use for all
	 * non-STOMP messages.
	 * 
	 * @param stompMessage
	 *          the stomp message
	 * @param hostPort
	 *          the host port
	 */
	public abstract void sendMessage(String stompMessage, HostPort hostPort) throws InterceptException;

	/**
	 * Closes the connection to the STOMP server or client.
	 * 
	 * @param hostPort
	 *          the host port
	 */
	public abstract void closeConnection(HostPort hostPort);

	/**
	 * Connects to a STOMP server or client as specified by configuration.
	 * 
	 * @throws Exception
	 *           the exception
	 */
	public abstract void connect() throws Exception;

	/**
	 * Shuts down the underlying connection technology.
	 * 
	 * @throws Exception
	 *           the exception
	 */
	public abstract void shutdown() throws Exception;

	/**
	 * Returns true if a connection exists and is active.
	 * 
	 * @param hostPort
	 *          the host port
	 * @return true, if is connected
	 */
	public abstract boolean isConnected(HostPort hostPort);

	/**
	 * If true the gateway will shut down when all sessions are terminated.
	 * Typically clients will be set to true, servers to false (the default).
	 * 
	 * @return
	 */
	public boolean isAutoShutdown() {
		return autoShutdown;
	}

	public void setAutoShutdown(boolean autoShutdown) {
		this.autoShutdown = autoShutdown;
	}

	public int getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}
}
