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
package asia.stampy.examples.system.client.mina;

import asia.stampy.client.listener.validate.ClientMessageValidationListener;
import asia.stampy.client.mina.RawClientMinaHandler;
import asia.stampy.client.mina.connected.MinaConnectedMessageListener;
import asia.stampy.client.mina.disconnect.MinaDisconnectListenerAndInterceptor;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.examples.client.mina.MinaAutoTerminatingClientGateway;
import asia.stampy.examples.common.IDontNeedSecurity;

/**
 * This class programmatically initializes the Stampy classes required for this
 * example, which tests the functionality of the various
 * {@link StampyMessageListener} implementations for a STOMP 1.2 compliant
 * client communicating with a compliant server. It is expected that a DI
 * framework such as <a href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemMinaClientInitializer {

  /**
   * Initialize.
   * 
   * @return the client mina message gateway
   */
  public static AbstractStampyMessageGateway initialize() {
    StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    MinaAutoTerminatingClientGateway gateway = new MinaAutoTerminatingClientGateway();
    gateway.setPort(1234);
    gateway.setHost("localhost");
    gateway.setHeartbeat(1000);

    RawClientMinaHandler handler = new RawClientMinaHandler();
    handler.setHeartbeatContainer(heartbeatContainer);
    handler.setGateway(gateway);

    gateway.addMessageListener(new IDontNeedSecurity());
    
    gateway.addMessageListener(new ClientMessageValidationListener());

    MinaConnectedMessageListener cml = new MinaConnectedMessageListener();
    cml.setHeartbeatContainer(heartbeatContainer);
    cml.setGateway(gateway);
    gateway.addMessageListener(cml);

    MinaDisconnectListenerAndInterceptor disconnect = new MinaDisconnectListenerAndInterceptor();
    disconnect.setCloseOnDisconnectMessage(false);
    gateway.addMessageListener(disconnect);
    gateway.addOutgoingMessageInterceptor(disconnect);
    disconnect.setGateway(gateway);

    gateway.setHandler(handler);

    return gateway;

  }
}
