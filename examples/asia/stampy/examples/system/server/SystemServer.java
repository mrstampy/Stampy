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

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.server.netty.SystemNettyServerInitializer;
import asia.stampy.server.message.message.MessageMessage;

/**
 * The Class SystemServer.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemServer {

  private AbstractStampyMessageGateway gateway;

  private int ackCount;

  /**
   * Inits.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(SystemNettyServerInitializer.initialize());

    gateway.addMessageListener(new StampyMessageListener() {

      @Override
      public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
        switch (message.getMessageType()) {
        case ABORT:
          break;
        case ACK:
          ackCount++;
          break;
        case BEGIN:
          break;
        case COMMIT:
          break;
        case CONNECT:
          break;
        case DISCONNECT:
          break;
        case NACK:
          break;
        case SEND:
          break;
        case STOMP:
          break;
        case SUBSCRIBE:
          ackCount = 0;
          sendMessages(((SubscribeMessage) message).getHeader().getId(), hostPort);
          break;
        case UNSUBSCRIBE:
          System.out.println("Unsubscribe received, with " + ackCount + " acks");
          break;
        default:
          break;

        }
      }

      @Override
      public boolean isForMessage(StampyMessage<?> message) {
        return true;
      }

      @Override
      public StompMessageType[] getMessageTypes() {
        return StompMessageType.values();
      }
    });

    gateway.connect();
    System.out.println("Stampy system server started");
  }

  private void sendMessages(String id, HostPort hostPort) throws InterceptException {
    for (int i = 0; i < 100; i++) {
      String msgId = Integer.toString(i);
      MessageMessage message = new MessageMessage("destination", msgId, id);
      message.getHeader().setAck(msgId);
      gateway.sendMessage(message, hostPort);
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

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    SystemServer server = new SystemServer();
    try {
      server.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
