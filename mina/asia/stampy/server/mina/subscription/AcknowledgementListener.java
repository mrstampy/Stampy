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
package asia.stampy.server.mina.subscription;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.ack.AckHeader;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.nack.NackHeader;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving acknowledgement events.
 * The class that is interested in processing a acknowledgement
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addAcknowledgementListener<code> method. When
 * the acknowledgement event occurs, that object's appropriate
 * method is invoked.
 *
 * @see AcknowledgementEvent
 */
@Resource
public class AcknowledgementListener implements StampyMinaMessageListener {
	private static final StompMessageType[] TYPES = { StompMessageType.ACK, StompMessageType.NACK };

	private MessageInterceptor interceptor;
	private StampyAcknowledgementHandler handler;

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
		case ACK:
			evaluateAck(((AckMessage) message).getHeader(), hostPort);
			break;
		case NACK:
			evaluateNack(((NackMessage) message).getHeader(), hostPort);
			break;
		default:
			break;

		}
	}

	private void evaluateNack(NackHeader header, HostPort hostPort) throws Exception {
		String id = header.getId();
		if (getInterceptor().hasMessageAck(id, hostPort)) {
			getInterceptor().clearMessageAck(id, hostPort);
			getHandler().nackReceived(id, header.getReceipt(), header.getTransaction());
		} else {
			throw new UnexpectedAcknowledgementException("No NACK message expected, yet received id " + id + " from "
					+ hostPort);
		}
	}

	private void evaluateAck(AckHeader header, HostPort hostPort) throws Exception {
		String id = header.getId();
		if (getInterceptor().hasMessageAck(id, hostPort)) {
			getInterceptor().clearMessageAck(id, hostPort);
			getHandler().ackReceived(id, header.getReceipt(), header.getTransaction());
		} else {
			throw new UnexpectedAcknowledgementException("No ACK message expected, yet received id " + id + " from "
					+ hostPort);
		}
	}

	/**
	 * Gets the interceptor.
	 *
	 * @return the interceptor
	 */
	public MessageInterceptor getInterceptor() {
		return interceptor;
	}

	/**
	 * Sets the interceptor.
	 *
	 * @param interceptor the new interceptor
	 */
	public void setInterceptor(MessageInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * Gets the handler.
	 *
	 * @return the handler
	 */
	public StampyAcknowledgementHandler getHandler() {
		return handler;
	}

	/**
	 * Sets the handler.
	 *
	 * @param adapter the new handler
	 */
	public void setHandler(StampyAcknowledgementHandler adapter) {
		this.handler = adapter;
	}

}
