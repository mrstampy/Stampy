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
package asia.stampy.examples.remote.exe.common;

import org.apache.commons.lang.StringUtils;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;
import asia.stampy.server.mina.ServerMinaMessageGateway;

// TODO: Auto-generated Javadoc
/**
 * Executes a {@link Remoteable} object and if a receipt has been specified,
 * returns a RECEIPT message. An ERROR message is returned to the client should
 * anything unexpected occur.
 */
public class RemoteExecutor {

	private ServerMinaMessageGateway gateway;

	/**
	 * Process stomp message.
	 * 
	 * @param message
	 *          the message
	 * @param hostPort
	 *          the host port
	 * @return true, if successful
	 * @throws Exception
	 *           the exception
	 */
	public boolean processStompMessage(SendMessage message, HostPort hostPort) throws Exception {
		try {
			Remoteable remoteable = message.getBody();

			remoteable.setProperties(message.getHeader().getHeaders());

			boolean b = remoteable.execute();

			sendSuccess(message, hostPort);

			return b;
		} catch (Exception e) {
			sendError(message, e, hostPort);
		}

		return false;
	}

	private void sendSuccess(SendMessage message, HostPort hostPort) throws InterceptException {
		String receiptId = message.getHeader().getReceipt();

		if (StringUtils.isEmpty(receiptId)) return;

		ReceiptMessage receipt = new ReceiptMessage(receiptId);

		getGateway().sendMessage(receipt, hostPort);
	}

	private void sendError(SendMessage message, Exception e, HostPort hostPort) throws InterceptException {
		String receipt = message.getHeader().getReceipt();

		ErrorMessage error = new ErrorMessage(StringUtils.isEmpty(receipt) ? "n/a" : receipt);
		error.getHeader().setMessageHeader(
				"Could not execute " + message.getBody().getClass().getCanonicalName() + " - " + e.getMessage());
		error.getHeader().setContentType(AbstractBodyMessage.JAVA_BASE64_MIME_TYPE);
		error.setBody(e);

		getGateway().sendMessage(error, hostPort);
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
	 * @param messageSender
	 *          the new gateway
	 */
	public void setGateway(ServerMinaMessageGateway messageSender) {
		this.gateway = messageSender;
	}
}
