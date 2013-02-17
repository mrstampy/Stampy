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
package asia.stampy.client.listener.connected;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.connected.ConnectedMessage;

/**
 * This class intercepts incoming {@link StompMessageType#CONNECTED} from a
 * STOMP 1.2 server and starts a heartbeat, if requested.
 * 
 * <i>CONNECT heart-beat:[cx],[cy] <br>
 * CONNECTED: heart-beat:[sx],[sy]<br>
 * <br>
 * For heart-beats from the client to the server: if [cx] is 0 (the client
 * cannot send heart-beats) or [sy] is 0 (the server does not want to receive
 * heart-beats) then there will be none otherwise, there will be heart-beats
 * every MAX([cx],[sy]) milliseconds In the other direction, [sx] and [cy] are
 * used the same way.</i>
 * 
 * @see StampyHeartbeatContainer
 * @see PaceMaker
 */
@StampyLibrary(libraryName = "stampy-client-server")
public abstract class AbstractConnectedMessageListener<CLNT extends AbstractStampyMessageGateway> implements
    StampyMessageListener {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static StompMessageType[] TYPES = { StompMessageType.CONNECTED };

  private StampyHeartbeatContainer heartbeatContainer;

  private CLNT gateway;

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.gateway.StampyMessageListener#getMessageTypes()
   */
  @Override
  public StompMessageType[] getMessageTypes() {
    return TYPES;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.StampyMessageListener#isForMessage(asia.stampy
   * .common.message.StampyMessage)
   */
  @Override
  public boolean isForMessage(StampyMessage<?> message) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.gateway.StampyMessageListener#messageReceived(asia.
   * stampy.common.message.StampyMessage, asia.stampy.common.HostPort)
   */
  @Override
  public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
    log.debug("Received connect message {} from {}", message, hostPort);
    ConnectedMessage cm = (ConnectedMessage) message;

    int requested = cm.getHeader().getIncomingHeartbeat();

    if (requested <= 0 || gateway.getHeartbeat() <= 0) return;

    int heartbeat = Math.max(requested, gateway.getHeartbeat());

    log.info("Starting heartbeats for {} at {} ms intervals", hostPort, heartbeat);

    getHeartbeatContainer().start(hostPort, getGateway(), heartbeat);
  }

  /**
   * Gets the heartbeat container.
   * 
   * @return the heartbeat container
   */
  public StampyHeartbeatContainer getHeartbeatContainer() {
    return heartbeatContainer;
  }

  /**
   * Inject the {@link HeartbeatContainer} on system startup.
   * 
   * @param heartbeatContainer
   *          the new heartbeat container
   */
  public void setHeartbeatContainer(StampyHeartbeatContainer heartbeatContainer) {
    this.heartbeatContainer = heartbeatContainer;
  }

  /**
   * Gets the message gateway.
   * 
   * @return the message gateway
   */
  public CLNT getGateway() {
    return gateway;
  }

  /**
   * Inject the client {@link AbstractStampyMessageGateway} on system startup.
   * 
   * @param gateway
   *          the new message gateway
   */
  public void setGateway(CLNT gateway) {
    this.gateway = gateway;
  }

}
