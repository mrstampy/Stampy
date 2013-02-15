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
package asia.stampy.examples.remote.exe.log4j.client;

import org.apache.log4j.Level;

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.remote.exe.log4j.client.netty.NettyInitializer;
import asia.stampy.examples.remote.exe.log4j.common.Log4jLevelChanger;

/**
 * It is intended that binary payloads in STOMP messages are sent to a server be
 * persisted in some manner ie. written to a database after passing security
 * checks. This class demonstrates how to execute a Java binary payload on a
 * remote server using the STOMP 1.2 protocol. Even something as simple as a log
 * level change can have the effect of filling up the logs & crashing the
 * server. DON'T DO THIS.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class Log4jLevelChangerTestClient {
  private AbstractStampyMessageGateway gateway;

  /**
   * Inits the.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(NettyInitializer.initialize());
    gateway.addMessageListener(new TestClientMessageListener());
    gateway.connect();
    gateway.broadcastMessage(new ConnectMessage("localhost"));
  }

  /**
   * Disconnect.
   * 
   * @throws Exception
   *           the exception
   */
  public void disconnect() throws Exception {
    gateway.broadcastMessage(new DisconnectMessage());
  }

  private void sendSendMessage(Log4jLevelChanger levelChanger) throws InterceptException {
    SendMessage message = new SendMessage("over/there", "blah");
    message.getHeader().setContentType(AbstractBodyMessage.JAVA_BASE64_MIME_TYPE);
    message.setBody(levelChanger);
    getGateway().broadcastMessage(message);
  }

  private Log4jLevelChanger createLevelChanger(String logger, Level level) {
    return new Log4jLevelChanger(logger, level);
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
    Log4jLevelChangerTestClient client = new Log4jLevelChangerTestClient();
    try {
      client.init();
      sendMessage(client, Level.ALL);
      sendMessage(client, Level.TRACE);
      sendMessage(client, Level.DEBUG);
      sendMessage(client, Level.INFO);
      sendMessage(client, Level.WARN);
      sendMessage(client, Level.ERROR);
      sendMessage(client, Level.FATAL);
      sendMessage(client, Level.OFF);
      client.disconnect();
      client.getGateway().shutdown();
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sendMessage(Log4jLevelChangerTestClient client, Level level) throws InterruptedException,
      InterceptException {
    client.sendSendMessage(client.createLevelChanger("asia.stampy.examples.remote.exe.log4j.server", level));
    Thread.sleep(500);
  }
}
