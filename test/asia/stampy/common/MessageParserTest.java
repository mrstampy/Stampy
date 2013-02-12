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

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

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
import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

/**
 * The Class MessageParserTest.
 */
public class MessageParserTest {

  private StompMessageParser parser = new StompMessageParser();

  /**
   * Test connect message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testConnectMessageParsing() throws Exception {
    testEquals(new ConnectMessage("1.2", "burt.alexander"));
  }

  /**
   * Test send message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testSendMessageParsing() throws Exception {
    SendMessage message = new SendMessage("over/there", "receiptId");
    message.setMimeType("text/plain", "UTF-8");
    message.setBody("The body");

    testEquals(message);
  }

  /**
   * Test send message parsing byte array.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testSendMessageParsingByteArray() throws Exception {
    SendMessage message = new SendMessage("over/there", "receiptId");
    message.setMimeType("application/jpeg", "UTF-8");
    message.setBody(new HostPort("burt.alexander", 1234));

    testEquals(message);
  }

  /**
   * Test subscribe message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testSubscribeMessageParsing() throws Exception {
    testEquals(new SubscribeMessage("over/there", "12345"));
  }

  /**
   * Test connected message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testConnectedMessageParsing() throws Exception {
    testEquals(new ConnectedMessage("1.2"));
  }

  /**
   * Test error message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testErrorMessageParsing() throws Exception {
    ErrorMessage message = new ErrorMessage();
    message.setBody("The body");

    testEquals(message);
  }

  /**
   * Test ack parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testAckParsing() throws Exception {
    testEquals(new AckMessage("12345"));
  }

  /**
   * Test nack parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testNackParsing() throws Exception {
    testEquals(new NackMessage("12345"));
  }

  /**
   * Test disconnect parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testDisconnectParsing() throws Exception {
    testEquals(new DisconnectMessage());
  }

  /**
   * Test unsubscribe parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testUnsubscribeParsing() throws Exception {
    testEquals(new UnsubscribeMessage("12345"));
  }

  /**
   * Test abort parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testAbortParsing() throws Exception {
    testEquals(new AbortMessage("transaction"));
  }

  /**
   * Test commit parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testCommitParsing() throws Exception {
    testEquals(new CommitMessage("transaction"));
  }

  /**
   * Test subscribe parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testSubscribeParsing() throws Exception {
    testEquals(new SubscribeMessage("over/there", "12345"));
  }

  /**
   * Test begin parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testBeginParsing() throws Exception {
    testEquals(new BeginMessage("transaction"));
  }

  /**
   * Test receipt parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testReceiptParsing() throws Exception {
    testEquals(new ReceiptMessage("12345"));
  }

  /**
   * Test message parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testMessageParsing() throws Exception {
    MessageMessage mm = new MessageMessage("over/there", "12345", "54321");
    mm.setBody("This is the body");

    testEquals(mm);
  }

  /**
   * Test stomp parsing.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void testStompParsing() throws Exception {
    testEquals(new StompMessage("burt.alexander"));
  }

  private void testEquals(AbstractMessage<?> message) throws UnparseableException {
    String stomp = message.toStompMessage(true);

    AbstractMessage<?> parsed = parser.parseMessage(stomp);

    assertTrue(message.equals(parsed));
  }
}
