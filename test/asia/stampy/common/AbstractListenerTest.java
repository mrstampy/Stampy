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
package asia.stampy.common;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.mockito.Mock;

import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.begin.BeginMessage;
import asia.stampy.client.message.commit.CommitMessage;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

public abstract class AbstractListenerTest {
  protected HostPort hostPort = new HostPort("burt.alexander", 9999);

  @Mock
  protected TestClientMessageGateway clientGateway;

  @Mock
  protected TestServerMessageGateway serverGateway;
  
  @Mock
  protected StampyHeartbeatContainer heartbeatContainer;

  protected void testTypes(StampyMessageListener listener, StompMessageType[] expecteds) {
    StompMessageType[] actuals = listener.getMessageTypes();

    testTypes(expecteds, actuals);
  }

  protected void testTypes(StampyOutgoingMessageInterceptor interceptor, StompMessageType[] expecteds) {
    StompMessageType[] actuals = interceptor.getMessageTypes();

    testTypes(expecteds, actuals);
  }

  protected StampyMessage<?> getMessage(StompMessageType type) {
    switch (type) {
    case ABORT:
      return new AbortMessage("transaction");
    case ACK:
      return new AckMessage("id");
    case BEGIN:
      return new BeginMessage("transaction");
    case COMMIT:
      return new CommitMessage("transaction");
    case NACK:
      return new NackMessage("id");
    case SEND:
      return new SendMessage("destination", "receiptId");
    case SUBSCRIBE:
      return new SubscribeMessage("destination", "id");
    case UNSUBSCRIBE:
      return new UnsubscribeMessage("id");
    case CONNECT:
      return new ConnectMessage("host");
    case CONNECTED:
      return new ConnectedMessage("1.2");
    case DISCONNECT:
      return new DisconnectMessage();
    case ERROR:
      return new ErrorMessage("receiptId");
    case MESSAGE:
      return new MessageMessage("destination", "messageId", "subscription");
    case RECEIPT:
      return new ReceiptMessage("receiptId");
    case STOMP:
      return new StompMessage("host");
    default:
      throw new IllegalArgumentException(type + " is not recognized");
    }
  }

  private void testTypes(StompMessageType[] expecteds, StompMessageType[] actuals) {
    assertEquals(expecteds.length, actuals.length);

    for (StompMessageType expected : expecteds) {
      testTypes(expected, actuals);
    }
  }

  private void testTypes(StompMessageType expected, StompMessageType[] actuals) {
    boolean exists = false;
    for (StompMessageType actual : actuals) {
      if (actual.equals(expected)) exists = true;
    }

    assertTrue(exists);
  }

}
