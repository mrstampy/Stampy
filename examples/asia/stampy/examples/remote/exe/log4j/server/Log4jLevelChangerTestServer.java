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
package asia.stampy.examples.remote.exe.log4j.server;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.examples.remote.exe.log4j.server.netty.NettyInitializer;

/**
 * It is intended that binary payloads in STOMP messages are sent to a server be
 * persisted in some manner ie. written to a database after passing security
 * checks. This class demonstrates how to execute a Java binary payload on a
 * remote server using the STOMP 1.2 protocol. Even something as simple as a log
 * level change can have the effect of filling up the logs & crashing the
 * server. DON'T DO THIS.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class Log4jLevelChangerTestServer {
  private AbstractStampyMessageGateway gateway;

  /**
   * Inits.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(NettyInitializer.initialize());
    gateway.addMessageListener(new TestServerMessageListener());
    gateway.connect();
    System.out.println("Stampy server started");
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    Log4jLevelChangerTestServer server = new Log4jLevelChangerTestServer();
    try {
      server.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
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

}
