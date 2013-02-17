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

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.loadtest.server.TestServer;
import asia.stampy.server.message.error.ErrorMessage;

/**
 * This listener sends a bunch of messages to the {@link TestServer} to load
 * test.
 * 
 * @see TestClientMessageEvent
 */
@StampyLibrary(libraryName = "stampy-examples")
public class TestClientMessageListener implements StampyMessageListener {
  private AbstractStampyMessageGateway gateway;

  private AtomicInteger receipts = new AtomicInteger();

  private boolean connected = false;

  private long start;
  private long end;

  private Object waiter = new Object();

  // the number of messages to send.
  private int times = 1000000;

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.StampyMinaMessageListener#messageReceived(asia.
   * stampy.common.message.StampyMessage,
   * org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
   */
  @Override
  public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
    switch (message.getMessageType()) {
    case CONNECTED:
      connected = true;
      start = System.nanoTime();
      System.out.println("Sending " + times + " messages to the server, receipts requested...");
      sendAcks(); // should be a threaded operation in prod
      break;
    case ERROR:
      System.out.println("Unexpected error " + ((ErrorMessage) message).getHeader().getMessageHeader());
      break;
    case RECEIPT:
      receipts.getAndIncrement();

      if (receipts.get() == times) {
        synchronized (waiter) {
          waiter.notifyAll();
        }
      }

      break;
    default:
      System.out.println("Unexpected message " + message.getMessageType());
      break;

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.StampyMinaMessageListener#isForMessage(asia.stampy
   * .common.message.StampyMessage)
   */
  @Override
  public boolean isForMessage(StampyMessage<?> message) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.mina.StampyMinaMessageListener#getMessageTypes()
   */
  @Override
  public StompMessageType[] getMessageTypes() {
    return StompMessageType.values();
  }

  /**
   * Disconnect, not a method one would normally see in an implementation of.
   * 
   * @throws Exception
   *           the exception {@link StampyMessageListener}
   */
  public void disconnect() throws Exception {
    synchronized (waiter) {
      waiter.wait();
    }
    gateway.broadcastMessage(new DisconnectMessage());
    end = System.nanoTime();
  }

  /**
   * Stats.
   */
  public void stats() {
    System.out.println("# of receipts: " + receipts.get());
    System.out.println("Connected message? " + connected);
    long diff = end - start;
    System.out.println("Nano time elapsed: " + diff);
    BigDecimal bd = new BigDecimal(diff);
    int divisor = (times + receipts.get()) * 1000;
    bd = bd.divide(new BigDecimal(divisor), 7, BigDecimal.ROUND_HALF_UP);
    System.out.println("Micro seconds per message: " + bd.doubleValue());
  }

  private void sendAcks() throws Exception {
    Thread thread = new Thread("Ack Thread") {
      public void run() {
        for (int i = 1; i <= times; i++) {
          try {
            sendAck(i);
          } catch (InterceptException e) {
            e.printStackTrace();
          }
        }
      }
    };
    
    thread.start();
  }

  private void sendAck(int i) throws InterceptException {
    String id = Integer.toString(i);
    AckMessage ack = new AckMessage(id);
    ack.getHeader().setReceipt(id);
    ack.getHeader().setTransaction(id);

    gateway.broadcastMessage(ack);
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
