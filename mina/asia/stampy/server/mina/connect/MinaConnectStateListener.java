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
package asia.stampy.server.mina.connect;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.server.listener.connect.AbstractConnectStateListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

/**
 * This class ensures that a {@link StompMessageType#CONNECT} or
 * {@link StompMessageType#STOMP} frame is the first frame a client sends, that
 * no additional connect frames are sent, and that a
 * {@link StompMessageType#DISCONNECT} frame initializes the state.<br>
 * <br>
 */
@Resource
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class MinaConnectStateListener extends AbstractConnectStateListener<ServerMinaMessageGateway> {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  protected void ensureCleanup() {
    getGateway().addServiceListener(new MinaServiceAdapter() {

      @Override
      public void sessionDestroyed(IoSession session) throws Exception {
        HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
        if (connectedClients.contains(hostPort)) {
          log.debug("{} session terminated with outstanding connection, cleaning up", hostPort);
          connectedClients.remove(hostPort);
        }
      }
    });
  }

}
