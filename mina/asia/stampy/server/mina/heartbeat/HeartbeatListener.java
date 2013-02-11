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
package asia.stampy.server.mina.heartbeat;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

/**
 * The listener interface for receiving heartbeat events. The class that is
 * interested in processing a heartbeat event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addHeartbeatListener<code> method. When
 * the heartbeat event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see HeartbeatEvent
 */
@Resource
public class HeartbeatListener implements StampyMinaMessageListener {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static StompMessageType[] TYPES = { StompMessageType.CONNECT, StompMessageType.STOMP,
      StompMessageType.DISCONNECT };

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
    return StringUtils.isNotEmpty(message.getHeader().getHeaderValue(ConnectHeader.HEART_BEAT))
        || isDisconnectMessage(message);
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
    if (isDisconnectMessage(message)) {
      getHeartbeatContainer().remove(hostPort);
      return;
    }

    ConnectHeader header = getConnectHeader(message);

    int requested = header.getIncomingHeartbeat();
    if (getMessageGateway().getHeartbeat() <= 0 || requested <= 0) return;

    int heartbeat = Math.max(requested, getMessageGateway().getHeartbeat());

    log.info("Starting heartbeats for {} at {} ms intervals", hostPort, heartbeat);
    PaceMaker paceMaker = new PaceMaker(heartbeat);
    paceMaker.setHostPort(hostPort);
    paceMaker.setGateway(getMessageGateway());
    paceMaker.start();

    getHeartbeatContainer().add(hostPort, paceMaker);
  }

  /**
   * Reset heartbeat.
   * 
   * @param hostPort
   *          the host port
   */
  public void resetHeartbeat(HostPort hostPort) {
    getHeartbeatContainer().reset(hostPort);
  }

  private ConnectHeader getConnectHeader(StampyMessage<?> message) {
    return isConnectMessage(message) ? ((ConnectMessage) message).getHeader() : ((StompMessage) message).getHeader();
  }

  private boolean isConnectMessage(StampyMessage<?> message) {
    return StompMessageType.CONNECT.equals(message.getMessageType());
  }

  private boolean isDisconnectMessage(StampyMessage<?> message) {
    return StompMessageType.DISCONNECT.equals(message.getMessageType());
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
  public void setGateway(AbstractStampyMessageGateway messageGateway) {
    this.messageGateway = messageGateway;
  }

}
