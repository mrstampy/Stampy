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
package asia.stampy.server.netty.transaction;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.listener.transaction.AbstractTransactionListener;
import asia.stampy.server.netty.ServerNettyMessageGateway;

/**
 * This class manages transactional boundaries, ensuring that a transaction has
 * been started prior to an {@link StompMessageType#ABORT} or.
 * 
 * {@link StompMessageType#COMMIT} and that a transaction is began only once.
 */
@Resource
@StampyLibrary(libraryName = "stampy-NETTY-client-server-RI")
public class NettyTransactionListener extends AbstractTransactionListener<ServerNettyMessageGateway> {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.server.listener.transaction.AbstractTransactionListener#
   * ensureCleanup()
   */
  @Override
  protected void ensureCleanup() {
    getGateway().addHandler(new SimpleChannelUpstreamHandler() {
      public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HostPort hostPort = new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
        if (activeTransactions.containsKey(hostPort)) {
          log.debug("{} session terminated with outstanding transaction, cleaning up", hostPort);
          activeTransactions.remove(hostPort);
        }
      }
    });
  }

}
