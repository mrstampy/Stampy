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

import asia.stampy.common.message.StampyMessage;

/**
 * A StampyMessageGateway is the interface between the technology used to
 * connect to a STOMP implementation and the Stampy library. It is the class
 * through which STOMP messages are sent and received.<br>
 * <br>
 * Subclasses are singletons; wire into the system appropriately.
 */
public abstract class AbstractStampyMessageGateway {

	/**
	 * Sends a {@link StampyMessage} to the specified {@link HostPort}. Use this
	 * method for all STOMP messages.
	 * 
	 * @param message
	 *          the message
	 * @param hostPort
	 *          the host port
	 */
	public void sendMessage(StampyMessage<?> message, HostPort hostPort) {
		sendMessage(message.toStompMessage(true), hostPort);
	}

	/**
	 * Broadcasts a {@link StampyMessage} to all connected clients from the server
	 * or to the server from a client. Use this method for all STOMP messages.
	 * 
	 * @param message
	 *          the message
	 */
	public void broadcastMessage(StampyMessage<?> message) {
		broadcastMessage(message.toStompMessage(true));
	}

	/**
	 * Broadcasts the specified String to all connections. Included for STOMP
	 * implementations which accept custom message types. Use for all non-STOMP
	 * messages.
	 * 
	 * @param stompMessage
	 *          the stomp message
	 */
	public abstract void broadcastMessage(String stompMessage);

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
	public abstract void sendMessage(String stompMessage, HostPort hostPort);

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
	 * Shutsdown the underlying connection technology.
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
}
