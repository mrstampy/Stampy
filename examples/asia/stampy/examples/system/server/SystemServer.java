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
package asia.stampy.examples.system.server;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemServer.
 */
public class SystemServer {

	private ServerMinaMessageGateway gateway;

	/**
	 * Inits.
	 * 
	 * @throws Exception
	 *           the exception
	 */
	public void init() throws Exception {
		setGateway(SystemServerInitializer.initialize());

		gateway.addMessageListener(new StampyMinaMessageListener() {

			@Override
			public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
				switch (message.getMessageType()) {
				case ABORT:
					break;
				case ACK:
					break;
				case BEGIN:
					break;
				case COMMIT:
					break;
				case CONNECT:
					break;
				case DISCONNECT:
					break;
				case NACK:
					break;
				case SEND:
					break;
				case STOMP:
					break;
				case SUBSCRIBE:
					break;
				case UNSUBSCRIBE:
					break;
				default:
					break;

				}
			}

			@Override
			public boolean isForMessage(StampyMessage<?> message) {
				return true;
			}

			@Override
			public StompMessageType[] getMessageTypes() {
				return StompMessageType.values();
			}
		});

		gateway.connect();
		System.out.println("Stampy system server started");
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
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SystemServer server = new SystemServer();
		try {
			server.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
