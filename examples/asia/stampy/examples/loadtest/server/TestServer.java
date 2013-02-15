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

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.examples.loadtest.server.netty.NettyInitializer;

/**
 * Receives message from a test client and sends receipts if requested.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class TestServer {
  private AbstractStampyMessageGateway gateway;

  /**
   * Inits the.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(NettyInitializer.initialize());

    gateway.addMessageListener(new TestServerMessageListener(), 1);

    gateway.connect();
    System.out.println("Stampy server started");
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
    TestServer server = new TestServer();
    try {
      server.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
