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
package asia.stampy.examples.loadtest.server.netty;

import asia.stampy.server.netty.ServerNettyMessageGateway;

/**
 * Receives message from a test client and sends receipts if requested.
 */
public class TestNettyServer {
  private ServerNettyMessageGateway gateway;

  /**
   * Inits the.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(Initializer.initialize());

    gateway.addMessageListener(new TestServerMessageListener());

    gateway.connect();
    System.out.println("Stampy server started");
  }

  /**
   * Gets the gateway.
   * 
   * @return the gateway
   */
  public ServerNettyMessageGateway getGateway() {
    return gateway;
  }

  /**
   * Sets the gateway.
   * 
   * @param gateway
   *          the new gateway
   */
  public void setGateway(ServerNettyMessageGateway gateway) {
    this.gateway = gateway;
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    TestNettyServer server = new TestNettyServer();
    try {
      server.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
