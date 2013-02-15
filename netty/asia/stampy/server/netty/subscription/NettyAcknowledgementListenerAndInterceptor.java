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
package asia.stampy.server.netty.subscription;

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
import asia.stampy.server.listener.subscription.AbstractAcknowledgementListenerAndInterceptor;
import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;
import asia.stampy.server.netty.ServerNettyMessageGateway;

/**
 * This class assists in the publication of {@link StompMessageType#MESSAGE}
 * messages for a subscription. If confirmation of the publication is requested
 * a timer is created to await receipt of the confirmation, and the appropriate
 * methods of the {@link StampyAcknowledgementHandler} implementation are
 * invoked.
 */
@Resource
@StampyLibrary(libraryName = "stampy-NETTY-client-server-RI")
public class NettyAcknowledgementListenerAndInterceptor extends
    AbstractAcknowledgementListenerAndInterceptor<ServerNettyMessageGateway> {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.server.listener.subscription.
   * AbstractAcknowledgementListenerAndInterceptor#ensureCleanup()
   */
  @Override
  protected void ensureCleanup() {
    getGateway().addHandler(new SimpleChannelUpstreamHandler() {
      public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HostPort hostPort = new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
        if (messages.containsKey(hostPort)) {
          log.debug("{} session terminated, cleaning up message interceptor", hostPort);
          messages.remove(hostPort);
        }
      }
    });
  }

}
