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

import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.examples.common.IDontNeedSecurity;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.connect.ConnectListener;
import asia.stampy.server.mina.heartbeat.HeartbeatListener;
import asia.stampy.server.mina.login.LoginMessageListener;
import asia.stampy.server.mina.subscription.AcknowledgementListenerAndInterceptor;
import asia.stampy.server.mina.transaction.TransactionListener;
import asia.stampy.server.mina.version.VersionListener;

/**
 * This class programmatically initializes the Stampy classes required for this
 * example which tests the functionality of the various
 * {@link StampyMinaMessageListener} implementations for a STOMP 1.2 compliant
 * server communicating with a compliant client. It is expected that a DI
 * framework such as <a href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
public class SystemServerInitializer {

  /**
   * Initialize.
   * 
   * @return the server mina message gateway
   */
  public static ServerMinaMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
    gateway.setPort(1234);
    gateway.setHeartbeat(1000);

    RawServerMinaHandler handler = new RawServerMinaHandler();
    handler.setHeartbeatContainer(heartbeatContainer);
    handler.setMessageGateway(gateway);
    
    handler.addMessageListener(new IDontNeedSecurity());

    handler.addMessageListener(new VersionListener());

    LoginMessageListener login = new LoginMessageListener();
    login.setGateway(gateway);
    login.setLoginHandler(new SystemLoginHandler());
    handler.addMessageListener(login);

    ConnectListener connect = new ConnectListener();
    connect.setGateway(gateway);
    handler.addMessageListener(connect);

    HeartbeatListener heartbeat = new HeartbeatListener();
    heartbeat.setHeartbeatContainer(heartbeatContainer);
    heartbeat.setGateway(gateway);
    handler.addMessageListener(heartbeat);

    TransactionListener transaction = new TransactionListener();
    transaction.setGateway(gateway);
    handler.addMessageListener(transaction);

    SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

    AcknowledgementListenerAndInterceptor acknowledgement = new AcknowledgementListenerAndInterceptor();
    acknowledgement.setHandler(sys);
    acknowledgement.setGateway(gateway);
    acknowledgement.setAckTimeoutMillis(200);
    handler.addMessageListener(acknowledgement);
    gateway.addOutgoingMessageInterceptor(acknowledgement);

    gateway.setHandler(handler);

    return gateway;
  }

}
