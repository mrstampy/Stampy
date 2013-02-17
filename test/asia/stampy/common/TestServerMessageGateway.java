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

import java.util.Set;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.interceptor.InterceptException;

public class TestServerMessageGateway extends AbstractStampyMessageGateway {

  public TestServerMessageGateway() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void broadcastMessage(String stompMessage) throws InterceptException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendMessage(String stompMessage, HostPort hostPort) throws InterceptException {
    // TODO Auto-generated method stub

  }

  @Override
  public void closeConnection(HostPort hostPort) {
    // TODO Auto-generated method stub

  }

  @Override
  public void connect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isConnected(HostPort hostPort) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Set<HostPort> getConnectedHostPorts() {
    // TODO Auto-generated method stub
    return null;
  }

}
