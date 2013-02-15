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
package asia.stampy.examples.loadtest.client;

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.examples.loadtest.client.netty.NettyInitializer;
import asia.stampy.examples.loadtest.server.TestServer;

/**
 * Sends many messages to a {@link TestServer}, prints stats on the operation
 * and terminates. Run the {@link TestServer} prior to running this client.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class TestClient {
  private AbstractStampyMessageGateway gateway;
  private TestClientMessageListener listener;

  /**
   * Inits the.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(NettyInitializer.initialize());
    listener = new TestClientMessageListener();
    listener.setGateway(gateway);
    gateway.addMessageListener(listener, 1);

    gateway.connect();
    gateway.broadcastMessage(new ConnectMessage("localhost"));
  }

  /**
   * Gets the gateway.
   * 
   * @return the gateway
   */
  public AbstractStampyMessageGateway getGateway() {
    return gateway;
  }

  /**
   * Sets the gateway.
   * 
   * @param gateway
   *          the new gateway
   */
  public void setGateway(AbstractStampyMessageGateway gateway) {
    this.gateway = gateway;
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    TestClient client = new TestClient();
    try {
      client.init();
      client.listener.disconnect();
      client.listener.stats();
      client.getGateway().shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
