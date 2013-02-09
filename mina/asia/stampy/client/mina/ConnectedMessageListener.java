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
package asia.stampy.client.mina;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.connected.ConnectedMessage;

/**
 * 
 */
@Resource
public class ConnectedMessageListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static StompMessageType[] TYPES = { StompMessageType.CONNECTED };

	private HeartbeatContainer heartbeatContainer;

	private AbstractStampyMessageGateway messageGateway;

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
		log.debug("Received connect message {} from {}", message, hostPort);
		ConnectedMessage cm = (ConnectedMessage) message;

		int requested = cm.getHeader().getIncomingHeartbeat();

		if (requested <= 0 || messageGateway.getHeartbeat() <= 0) return;

		int heartbeat = Math.max(requested, messageGateway.getHeartbeat());

		log.info("Starting heartbeats for {} at {} ms intervals", hostPort, heartbeat);
		PaceMaker paceMaker = new PaceMaker(heartbeat);
		paceMaker.setHostPort(hostPort);
		paceMaker.setMessageGateway(getMessageGateway());
		getHeartbeatContainer().add(hostPort, paceMaker);
	}

	/**
	 * Gets the heartbeat container.
	 * 
	 * @return the heartbeat container
	 */
	public HeartbeatContainer getHeartbeatContainer() {
		return heartbeatContainer;
	}

	/**
	 * Sets the heartbeat container.
	 * 
	 * @param heartbeatContainer
	 *          the new heartbeat container
	 */
	public void setHeartbeatContainer(HeartbeatContainer heartbeatContainer) {
		this.heartbeatContainer = heartbeatContainer;
	}

	/**
	 * Gets the message gateway.
	 * 
	 * @return the message gateway
	 */
	public AbstractStampyMessageGateway getMessageGateway() {
		return messageGateway;
	}

	/**
	 * Sets the message gateway.
	 * 
	 * @param messageGateway
	 *          the new message gateway
	 */
	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		this.messageGateway = messageGateway;
	}

}
