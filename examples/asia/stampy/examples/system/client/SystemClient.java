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
package asia.stampy.examples.system.client;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;
import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.begin.BeginMessage;
import asia.stampy.client.message.commit.CommitMessage;
import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeHeader.Ack;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.client.netty.SystemNettyClientInitializer;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

/**
 * The Class SystemClient.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemClient {

  private static final String CANNOT_BE_LOGGED_IN = "cannot be logged in";

  private static final String IS_ALREADY_LOGGED_IN = "is already logged in";

  private static final String ONLY_STOMP_VERSION_1_2_IS_SUPPORTED = "Only STOMP version 1.2 is supported";

  private static final String LOGIN_AND_PASSCODE_NOT_SPECIFIED = "login and passcode not specified";

  private static final String NOT_LOGGED_IN = "Not logged in";

  private static final StompMessageType[] CLIENT_TYPES = { ACK, NACK, SEND, ABORT, BEGIN, COMMIT, SUBSCRIBE,
      UNSUBSCRIBE };

  private AbstractStampyMessageGateway gateway;

  private ErrorMessage error;

  private ReceiptMessage receipt;

  private Object waiter = new Object();

  private boolean connected;

  private int messageCount;

  private HostPort hostPort;

  /**
   * Inits the.
   * 
   * @throws Exception
   *           the exception
   */
  public void init() throws Exception {
    setGateway(SystemNettyClientInitializer.initialize());
    gateway.addMessageListener(new StampyMessageListener() {

      @Override
      public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
        SystemClient.this.hostPort = hostPort;
        switch (message.getMessageType()) {
        case CONNECTED:
          connected = true;
          wakeup();
          break;
        case ERROR:
          setError((ErrorMessage) message);
          System.out.println("Error message received: " + message);
          wakeup();
          break;
        case MESSAGE:
          onMessage((MessageMessage) message);
          break;
        case RECEIPT:
          setReceipt((ReceiptMessage) message);
          wakeup();
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
  }

  /**
   * Test connect.
   * 
   * @throws Exception
   *           the exception
   */
  public void testConnect() throws Exception {
    for (int i = 0; i < CLIENT_TYPES.length; i++) {
      sendMessage(CLIENT_TYPES[i], Integer.toString(i));
      sleep();
      evaluateError(NOT_LOGGED_IN);
    }

    sendConnect("host");
    sleep();
    evaluateError(LOGIN_AND_PASSCODE_NOT_SPECIFIED);

    sendStomp("host");
    sleep();
    evaluateError(LOGIN_AND_PASSCODE_NOT_SPECIFIED);

    ConnectMessage message = new ConnectMessage("1.1", "burt.alexander");
    message.getHeader().setLogin(SystemLoginHandler.GOOD_USER);
    message.getHeader().setPasscode("pass");
    getGateway().broadcastMessage(message);
    sleep();
    evaluateError(ONLY_STOMP_VERSION_1_2_IS_SUPPORTED);

    message.getHeader().removeHeader(ConnectHeader.ACCEPT_VERSION);
    message.getHeader().setAcceptVersion("1.2");
    message.getHeader().setHeartbeat(500, 1000);
    getGateway().broadcastMessage(message);
    sleep();
    evaluateConnect();

    getGateway().broadcastMessage(message);
    sleep();
    evaluateError(IS_ALREADY_LOGGED_IN);

    sendDisconnect("Dissing");
    sleep();
    evaluateReceipt("Dissing");
    connected = false;
  }

  /**
   * Test login.
   * 
   * @throws Exception
   *           the exception
   */
  public void testLogin() throws Exception {
    badConnect();
    sleep();
    evaluateError(CANNOT_BE_LOGGED_IN);

    goodConnect();
    sleep();
    evaluateConnect();

    sendDisconnect("login");
    sleep();
    evaluateReceipt("login");
    connected = false;
  }

  public void testTransaction() throws Exception {
    goodConnect();
    sleep();
    evaluateConnect();

    sendAbort("id");
    sleep();
    evaluateError("Transaction not started");

    sendCommit("id");
    sleep();
    evaluateError("Transaction not started");

    sendBegin("begin");
    sleep();
    evaluateReceipt("begin");

    for (int i = 0; i < 100; i++) {
      String id = Integer.toString(i);
      sendSend(id);
      sleep();
      String receiptId = receipt.getHeader().getReceiptId();
      boolean expected = id.equals(receiptId);
      if (!expected) {
        System.err.println("Unexpected receipt id " + receiptId + ", expected " + id);
      }
    }

    sendCommit("begin");
    sleep();
    evaluateReceipt("begin");
  }

  public void testBadUser() throws Exception {
    sendDisconnect("login");
    sleep();
    evaluateReceipt("login");

    badConnect();
    sleep();
    evaluateError(CANNOT_BE_LOGGED_IN);

    badConnect();
    sleep();
    evaluateError(SystemLoginHandler.SEE_THE_SYSTEM_ADMINISTRATOR);

    System.out.println("Session is open (should not be)? " + gateway.isConnected(hostPort));
  }

  public void testSubscription() throws Exception {
    SubscribeMessage message = new SubscribeMessage("destination", "subscription");
    message.getHeader().setAck(Ack.clientIndividual);
    gateway.broadcastMessage(message);
    synchronized (waiter) {
      waiter.wait();
    }
  }

  private void onMessage(MessageMessage message) throws InterceptException, InterruptedException {
    messageCount++;
    if (messageCount < 100) {
      AckMessage ack = new AckMessage(message.getHeader().getMessageId());
      gateway.broadcastMessage(ack);
    }
    if (messageCount == 100) {
      System.out.println("Received all expected messages from subscription");
      sendUnsubscribe("subscription");
      Thread.sleep(250);
      synchronized (waiter) {
        waiter.notifyAll();
      }
    } else if (messageCount > 100) {
      throw new IllegalArgumentException("Extra message received");
    }
  }

  private void evaluateReceipt(String id) {
    String receiptId = receipt.getHeader().getReceiptId();
    boolean expected = id.equals(receiptId);

    System.out.println("Expected receipt id ? " + expected);
    System.out.println();
    receipt = null;
  }

  private void goodConnect() throws InterceptException {
    ConnectMessage message = new ConnectMessage("burt.alexander");
    message.getHeader().setLogin(SystemLoginHandler.GOOD_USER);
    message.getHeader().setPasscode("pass");
    message.getHeader().setHeartbeat(500, 1000);

    getGateway().broadcastMessage(message);
  }

  private void badConnect() throws InterceptException {
    ConnectMessage message = new ConnectMessage("burt.alexander");
    message.getHeader().setLogin(SystemLoginHandler.BAD_USER);
    message.getHeader().setPasscode("pass");
    message.getHeader().setHeartbeat(500, 1000);

    getGateway().broadcastMessage(message);
  }

  private void evaluateConnect() {
    System.out.println("Is connected? " + connected);
    System.out.println();
  }

  private void evaluateError(String messagePart) {
    String msg = error.getHeader().getMessageHeader();
    if (msg.contains(messagePart)) {
      System.out.println("Expected error message received");
    } else {
      System.err.println("Unexpected error message received");
    }
    System.out.println();
    error = null;
  }

  private void sendMessage(StompMessageType type, String id) throws InterceptException {
    switch (type) {
    case ABORT:
      sendAbort(id);
      break;
    case ACK:
      sendAck(id);
      break;
    case BEGIN:
      sendNack(id);
      break;
    case COMMIT:
      sendCommit(id);
      break;
    case CONNECT:
      sendConnect(id);
      break;
    case DISCONNECT:
      sendDisconnect(id);
      break;
    case NACK:
      sendNack(id);
      break;
    case SEND:
      sendSend(id);
      break;
    case STOMP:
      sendStomp(id);
      break;
    case SUBSCRIBE:
      sendSubscribe(id);
      break;
    case UNSUBSCRIBE:
      sendUnsubscribe(id);
      break;
    default:
      break;

    }
  }

  private void sendUnsubscribe(String id) throws InterceptException {
    UnsubscribeMessage message = new UnsubscribeMessage(id);
    getGateway().broadcastMessage(message);
  }

  private void sendSubscribe(String id) throws InterceptException {
    SubscribeMessage message = new SubscribeMessage("over/there", id);
    getGateway().broadcastMessage(message);
  }

  private void sendStomp(String id) throws InterceptException {
    StompMessage message = new StompMessage(id);
    getGateway().broadcastMessage(message);
  }

  private void sendSend(String id) throws InterceptException {
    SendMessage message = new SendMessage("over/there", id);
    message.getHeader().setReceipt(id);
    getGateway().broadcastMessage(message);
  }

  private void sendDisconnect(String id) throws InterceptException {
    DisconnectMessage message = new DisconnectMessage();
    message.getHeader().setReceipt(id);
    getGateway().broadcastMessage(message);
  }

  private void sendConnect(String id) throws InterceptException {
    ConnectMessage message = new ConnectMessage(id);
    getGateway().broadcastMessage(message);
  }

  private void sendCommit(String id) throws InterceptException {
    CommitMessage message = new CommitMessage(id);
    message.getHeader().setReceipt(id);
    getGateway().broadcastMessage(message);
  }

  private void sendNack(String id) throws InterceptException {
    NackMessage message = new NackMessage(id);
    getGateway().broadcastMessage(message);
  }

  private void sendAck(String id) throws InterceptException {
    AckMessage message = new AckMessage(id);
    getGateway().broadcastMessage(message);
  }

  private void sendAbort(String id) throws InterceptException {
    AbortMessage message = new AbortMessage(id);
    message.getHeader().setReceipt(id);
    getGateway().broadcastMessage(message);
  }

  private void sendBegin(String id) throws InterceptException {
    BeginMessage message = new BeginMessage(id);
    message.getHeader().setReceipt(id);
    getGateway().broadcastMessage(message);
  }

  private void sleep() throws InterruptedException {
    synchronized (waiter) {
      waiter.wait();
    }
  }

  private void wakeup() {
    synchronized (waiter) {
      waiter.notifyAll();
    }
  }

  /**
   * Gets the error.
   * 
   * @return the error
   */
  public ErrorMessage getError() {
    return error;
  }

  /**
   * Sets the error.
   * 
   * @param error
   *          the new error
   */
  public void setError(ErrorMessage error) {
    this.error = error;
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
    SystemClient client = new SystemClient();

    try {
      client.init();
      client.testConnect();
      client.testLogin();
      client.testTransaction();
      client.testSubscription();
      client.testBadUser();
      client.getGateway().shutdown();
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Gets the receipt.
   * 
   * @return the receipt
   */
  public ReceiptMessage getReceipt() {
    return receipt;
  }

  /**
   * Sets the receipt.
   * 
   * @param receipt
   *          the new receipt
   */
  public void setReceipt(ReceiptMessage receipt) {
    this.receipt = receipt;
  }

}
