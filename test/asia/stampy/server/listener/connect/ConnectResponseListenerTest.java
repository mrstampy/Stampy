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
package asia.stampy.server.listener.connect;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.common.AbstractListenerTest;
import asia.stampy.common.TestServerMessageGateway;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.connected.ConnectedMessage;

@RunWith(MockitoJUnitRunner.class)
public class ConnectResponseListenerTest extends AbstractListenerTest {

  private static final StompMessageType[] TYPES = new StompMessageType[] { StompMessageType.CONNECT,
      StompMessageType.STOMP };
  private AbstractConnectResponseListener<TestServerMessageGateway> connectResponse = new AbstractConnectResponseListener<TestServerMessageGateway>() {
  };

  @Before
  public void before() throws Exception {
    connectResponse.setGateway(serverGateway);
  }

  @Test
  public void testTypes() throws Exception {
    testTypes(connectResponse, TYPES);
  }

  @Test
  public void testResponseConnect() throws Exception {
    int cntr = 1;
    for (StompMessageType type : TYPES) {
      StampyMessage<?> message = getMessage(type);
      connectResponse.messageReceived(message, hostPort);
      verify(serverGateway, times(cntr)).sendMessage(any(ConnectedMessage.class), eq(hostPort));
      cntr++;
    }
  }

}
