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
package asia.stampy.client.listener.connected;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.common.AbstractListenerTest;
import asia.stampy.common.TestClientMessageGateway;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.connected.ConnectedMessage;

@RunWith(MockitoJUnitRunner.class)
public class ConnectedMessageListenerTest extends AbstractListenerTest {
  private AbstractConnectedMessageListener<TestClientMessageGateway> connected = new AbstractConnectedMessageListener<TestClientMessageGateway>() {
  };
  
  private ConnectedMessage message;

  @Before
  public void before() throws Exception {
    connected.setGateway(clientGateway);
    connected.setHeartbeatContainer(heartbeatContainer);
    message = new ConnectedMessage("1.2");
    when(clientGateway.getHeartbeat()).thenReturn(10);
  }

  @Test
  public void testTypes() throws Exception {
    testTypes(connected, new StompMessageType[] { StompMessageType.CONNECTED });
  }
  
  @Test
  public void testHeartbeatStartFaster() throws Exception {
    message.getHeader().setHeartbeat(5, 5);
    
    connected.messageReceived(message, hostPort);
    
    verify(heartbeatContainer).start(hostPort, clientGateway, 10);
  }
  
  @Test
  public void testHeartbeatStartSlower() throws Exception {
    message.getHeader().setHeartbeat(15, 15);
    
    connected.messageReceived(message, hostPort);
    
    verify(heartbeatContainer).start(hostPort, clientGateway, 15);
  }

}
