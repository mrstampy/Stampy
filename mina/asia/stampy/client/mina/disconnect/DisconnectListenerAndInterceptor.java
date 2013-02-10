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
package asia.stampy.client.mina.disconnect;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.AbstractOutgoingMessageInterceptor;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.receipt.ReceiptMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class DisconnectListenerAndInterceptor.
 */
@Resource
public class DisconnectListenerAndInterceptor extends AbstractOutgoingMessageInterceptor implements
		StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static StompMessageType[] TYPES = { StompMessageType.DISCONNECT, StompMessageType.RECEIPT };

	private boolean closeOnDisconnectMessage = true;
	private String receiptId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor
	 * #getMessageTypes()
	 */
	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor
	 * #isForMessage(asia.stampy.common.message.StampyMessage)
	 */
	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		switch (message.getMessageType()) {
		case DISCONNECT:
			boolean absent = StringUtils.isEmpty(getReceiptId());
			if (!absent) {
				log.warn("Outstanding receipt id {} in DisconnectListenerAndInterceptor, resetting", getReceiptId());
				setReceiptId((String) null);
			}
			return true;
		case RECEIPT:
			ReceiptMessage receipt = (ReceiptMessage) message;
			return StringUtils.isNotEmpty(getReceiptId()) && getReceiptId().equals(receipt.getHeader().getReceiptId());
		default:
			return false;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor
	 * #interceptMessage(asia.stampy.common.message.StampyMessage,
	 * asia.stampy.common.HostPort)
	 */
	@Override
	public void interceptMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
		switch (message.getMessageType()) {
		case DISCONNECT:
			setReceiptId((DisconnectMessage) message);
			break;
		default:
			return;

		}
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
		case RECEIPT:
			setReceiptId((String) null);
			if (isCloseOnDisconnectMessage()) {
				log.info("Receipt for disconnect message received, disconnecting");
				session.close(false);
			}
			break;
		default:
			return;

		}
	}

	private void setReceiptId(DisconnectMessage message) {
		String id = message.getHeader().getReceipt();
		log.info("Disconnect message intercepted, receipt id {}", id);
		setReceiptId(id);
	}

	/**
	 * Checks if is close on disconnect message.
	 * 
	 * @return true, if is close on disconnect message
	 */
	public boolean isCloseOnDisconnectMessage() {
		return closeOnDisconnectMessage;
	}

	/**
	 * Sets the close on disconnect message.
	 * 
	 * @param closeOnDisconnectMessage
	 *          the new close on disconnect message
	 */
	public void setCloseOnDisconnectMessage(boolean closeOnDisconnectMessage) {
		this.closeOnDisconnectMessage = closeOnDisconnectMessage;
	}

	/**
	 * Gets the receipt id.
	 * 
	 * @return the receipt id
	 */
	public String getReceiptId() {
		return receiptId;
	}

	/**
	 * Sets the receipt id.
	 * 
	 * @param receiptId
	 *          the new receipt id
	 */
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}

}
