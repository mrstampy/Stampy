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
package asia.stampy.client.listener.validate;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.CONNECT;
import static asia.stampy.common.message.StompMessageType.CONNECTED;
import static asia.stampy.common.message.StompMessageType.DISCONNECT;
import static asia.stampy.common.message.StompMessageType.ERROR;
import static asia.stampy.common.message.StompMessageType.MESSAGE;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.RECEIPT;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.STOMP;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.common.AbstractListenerTest;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

@RunWith(MockitoJUnitRunner.class)
public class ClientMessageValidationListenerTest extends AbstractListenerTest {

  private StompMessageType[] SERVER_TYPES = { RECEIPT, MESSAGE, CONNECTED, ERROR };
  private StompMessageType[] CLIENT_TYPES = { ABORT, ACK, BEGIN, COMMIT, CONNECT, DISCONNECT, NACK, SEND, STOMP,
      UNSUBSCRIBE, SUBSCRIBE };

  @Mock
  private StampyMessage<?> message;

  private ClientMessageValidationListener validation = new ClientMessageValidationListener();

  @Test
  public void testTypes() throws Exception {
    testTypes(validation, StompMessageType.values());
  }

  @Test
  public void testInvalidTypes() throws Exception {
    for (StompMessageType type : CLIENT_TYPES) {
      when(message.getMessageType()).thenReturn(type);
      try {
        validation.messageReceived(message, hostPort);
        fail("Should have thrown exception for client type " + type);
      } catch (IllegalArgumentException expected) {

      }
    }
  }

  @Test
  public void testValidTypes() throws Exception {
    int cntr = 1;
    for (StompMessageType type : SERVER_TYPES) {
      when(message.getMessageType()).thenReturn(type);
      validation.messageReceived(message, hostPort);
      verify(message, times(cntr)).validate();
      cntr++;
    }
  }

}
