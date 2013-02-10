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

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.AbstractOutgoingMessageInterceptor;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.server.message.message.MessageMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageInterceptor.
 */
@Resource
public class MessageInterceptor extends AbstractOutgoingMessageInterceptor {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final StompMessageType[] TYPES = { StompMessageType.MESSAGE };

	private Map<HostPort, Queue<String>> messages = new ConcurrentHashMap<>();

	private StampyAcknowledgementHandler handler;

	private Timer ackTimer = new Timer("Stampy Acknowledgement Timer", true);

	private long ackTimeoutMillis = 60000;

	/* (non-Javadoc)
	 * @see asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor#getMessageTypes()
	 */
	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor#isForMessage(asia.stampy.common.message.StampyMessage)
	 */
	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		MessageMessage msg = (MessageMessage) message;

		return StringUtils.isNotEmpty(msg.getHeader().getAck());
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor#interceptMessage(asia.stampy.common.message.StampyMessage, asia.stampy.common.HostPort)
	 */
	@Override
	public void interceptMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
		MessageMessage msg = (MessageMessage) message;

		String ack = msg.getHeader().getAck();

		Queue<String> queue = messages.get(hostPort);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<>();
			messages.put(hostPort, queue);
		}

		queue.add(ack);
		startTimerTask(hostPort, ack);
	}

	private void startTimerTask(final HostPort hostPort, final String ack) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Queue<String> q = messages.get(hostPort);
				if (!q.contains(ack)) return;

				getHandler().noAcknowledgementReceived(ack);
				q.remove(ack);
			}
		};
		
		ackTimer.schedule(task, getAckTimeoutMillis());
	}

	/**
	 * Checks for message ack.
	 *
	 * @param messageId the message id
	 * @param hostPort the host port
	 * @return true, if successful
	 */
	public boolean hasMessageAck(String messageId, HostPort hostPort) {
		Queue<String> ids = messages.get(hostPort);
		if (ids == null || ids.isEmpty()) return false;

		return ids.contains(messageId);
	}

	/**
	 * Clear message ack.
	 *
	 * @param messageId the message id
	 * @param hostPort the host port
	 */
	public void clearMessageAck(String messageId, HostPort hostPort) {
		Queue<String> ids = messages.get(hostPort);
		if (ids == null) return;

		ids.remove(messageId);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.message.interceptor.AbstractOutgoingMessageInterceptor#setGateway(asia.stampy.common.AbstractStampyMessageGateway)
	 */
	public void setGateway(AbstractStampyMessageGateway gateway) {
		super.setGateway(gateway);
		((AbstractStampyMinaMessageGateway) gateway).addServiceListener(new MinaServiceAdapter() {

			public void sessionDestroyed(IoSession session) throws Exception {
				HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
				if (messages.containsKey(hostPort)) {
					log.debug("{} session terminated, cleaning up message interceptor", hostPort);
					messages.remove(hostPort);
				}
			}
		});
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
	 * @param handler the new handler
	 */
	public void setHandler(StampyAcknowledgementHandler handler) {
		this.handler = handler;
	}

	/**
	 * Gets the ack timeout millis.
	 *
	 * @return the ack timeout millis
	 */
	public long getAckTimeoutMillis() {
		return ackTimeoutMillis;
	}

	/**
	 * Sets the ack timeout millis.
	 *
	 * @param ackTimeoutMillis the new ack timeout millis
	 */
	public void setAckTimeoutMillis(long ackTimeoutMillis) {
		this.ackTimeoutMillis = ackTimeoutMillis;
	}

}
