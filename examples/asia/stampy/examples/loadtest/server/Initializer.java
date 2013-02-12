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
package asia.stampy.examples.loadtest.server;

import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.examples.common.IDontNeedSecurity;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.connect.ConnectResponseListener;
import asia.stampy.server.mina.receipt.ReceiptListener;

/**
 * This class programmatically initializes the Stampy classes required for this
 * example. It is expected that a DI framework such as <a
 * href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
public class Initializer {

  /**
   * Initialize.
   * 
   * @return the server mina message gateway
   */
  public static ServerMinaMessageGateway initialize() {
    HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
    gateway.setPort(1234);

    RawServerMinaHandler handler = new RawServerMinaHandler();
    handler.setHeartbeatContainer(heartbeatContainer);
    handler.setGateway(gateway);
    
    handler.addMessageListener(new IDontNeedSecurity());
    
    ConnectResponseListener connectResponse = new ConnectResponseListener();
    connectResponse.setGateway(gateway);
    handler.addMessageListener(connectResponse);
    
    ReceiptListener receipt = new ReceiptListener();
    receipt.setGateway(gateway);
    handler.addMessageListener(receipt);

    gateway.setHandler(handler);

    return gateway;
  }

}
