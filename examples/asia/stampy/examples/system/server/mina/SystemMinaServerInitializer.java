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
package asia.stampy.examples.system.server.mina;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.examples.common.IDontNeedSecurity;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.connect.MinaConnectResponseListener;
import asia.stampy.server.mina.connect.MinaConnectStateListener;
import asia.stampy.server.mina.heartbeat.MinaHeartbeatListener;
import asia.stampy.server.mina.login.MinaLoginMessageListener;
import asia.stampy.server.mina.receipt.MinaReceiptListener;
import asia.stampy.server.mina.subscription.MinaAcknowledgementListenerAndInterceptor;
import asia.stampy.server.mina.transaction.MinaTransactionListener;

/**
 * This class programmatically initializes the Stampy classes required for this
 * example which tests the functionality of the various
 * {@link StampyMessageListener} implementations for a STOMP 1.2 compliant
 * server communicating with a compliant client. It is expected that a DI
 * framework such as <a href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemMinaServerInitializer {

  /**
   * Initialize.
   * 
   * @return the server mina message gateway
   */
  public static AbstractStampyMessageGateway initialize() {
    StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
    gateway.setPort(1234);
    gateway.setHeartbeat(1000);
    gateway.setAutoShutdown(true);
    gateway.addServiceListener(new MinaServiceAdapter() {

      @Override
      public void sessionDestroyed(IoSession session) throws Exception {
        System.out.println("Session destroyed, exiting...");
        System.exit(0);
      }

    });

    RawServerMinaHandler handler = new RawServerMinaHandler();
    handler.setHeartbeatContainer(heartbeatContainer);
    handler.setGateway(gateway);

    gateway.addMessageListener(new IDontNeedSecurity());
    
    gateway.addMessageListener(new ServerMessageValidationListener());

    gateway.addMessageListener(new VersionListener());

    MinaLoginMessageListener login = new MinaLoginMessageListener();
    login.setGateway(gateway);
    login.setLoginHandler(new SystemLoginHandler());
    gateway.addMessageListener(login);

    MinaConnectStateListener connect = new MinaConnectStateListener();
    connect.setGateway(gateway);
    gateway.addMessageListener(connect);

    MinaHeartbeatListener heartbeat = new MinaHeartbeatListener();
    heartbeat.setHeartbeatContainer(heartbeatContainer);
    heartbeat.setGateway(gateway);
    gateway.addMessageListener(heartbeat);

    MinaTransactionListener transaction = new MinaTransactionListener();
    transaction.setGateway(gateway);
    gateway.addMessageListener(transaction);

    SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

    MinaAcknowledgementListenerAndInterceptor acknowledgement = new MinaAcknowledgementListenerAndInterceptor();
    acknowledgement.setHandler(sys);
    acknowledgement.setGateway(gateway);
    acknowledgement.setAckTimeoutMillis(200);
    gateway.addMessageListener(acknowledgement);
    gateway.addOutgoingMessageInterceptor(acknowledgement);

    MinaReceiptListener receipt = new MinaReceiptListener();
    receipt.setGateway(gateway);
    gateway.addMessageListener(receipt);

    MinaConnectResponseListener connectResponse = new MinaConnectResponseListener();
    connectResponse.setGateway(gateway);
    gateway.addMessageListener(connectResponse);

    gateway.setHandler(handler);

    return gateway;
  }

}
