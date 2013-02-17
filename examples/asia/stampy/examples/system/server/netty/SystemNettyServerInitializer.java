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
package asia.stampy.examples.system.server.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.examples.common.IDontNeedSecurity;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.netty.ServerNettyChannelHandler;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import asia.stampy.server.netty.connect.NettyConnectResponseListener;
import asia.stampy.server.netty.connect.NettyConnectStateListener;
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener;
import asia.stampy.server.netty.login.NettyLoginMessageListener;
import asia.stampy.server.netty.receipt.NettyReceiptListener;
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor;
import asia.stampy.server.netty.transaction.NettyTransactionListener;

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
public class SystemNettyServerInitializer {

  /**
   * Initialize.
   * 
   * @return the server mina message gateway
   */
  public static AbstractStampyMessageGateway initialize() {
    StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
    gateway.setPort(1234);
    gateway.setHeartbeat(1000);
    gateway.setAutoShutdown(true);
    gateway.addHandler(new SimpleChannelUpstreamHandler() {
      public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("Session destroyed, exiting...");
        System.exit(0);
      }
    });

    ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
    channelHandler.setGateway(gateway);
    channelHandler.setHeartbeatContainer(heartbeatContainer);

    gateway.addMessageListener(new IDontNeedSecurity());
    
    gateway.addMessageListener(new ServerMessageValidationListener());

    gateway.addMessageListener(new VersionListener());

    NettyLoginMessageListener login = new NettyLoginMessageListener();
    login.setGateway(gateway);
    login.setLoginHandler(new SystemLoginHandler());
    gateway.addMessageListener(login);

    NettyConnectStateListener connect = new NettyConnectStateListener();
    connect.setGateway(gateway);
    gateway.addMessageListener(connect);

    NettyHeartbeatListener heartbeat = new NettyHeartbeatListener();
    heartbeat.setHeartbeatContainer(heartbeatContainer);
    heartbeat.setGateway(gateway);
    gateway.addMessageListener(heartbeat);

    NettyTransactionListener transaction = new NettyTransactionListener();
    transaction.setGateway(gateway);
    gateway.addMessageListener(transaction);

    SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

    NettyAcknowledgementListenerAndInterceptor acknowledgement = new NettyAcknowledgementListenerAndInterceptor();
    acknowledgement.setHandler(sys);
    acknowledgement.setGateway(gateway);
    acknowledgement.setAckTimeoutMillis(200);
    gateway.addMessageListener(acknowledgement);
    gateway.addOutgoingMessageInterceptor(acknowledgement);

    NettyReceiptListener receipt = new NettyReceiptListener();
    receipt.setGateway(gateway);
    gateway.addMessageListener(receipt);

    NettyConnectResponseListener connectResponse = new NettyConnectResponseListener();
    connectResponse.setGateway(gateway);
    gateway.addMessageListener(connectResponse);

    gateway.setHandler(channelHandler);

    return gateway;
  }

}
