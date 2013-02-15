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
package asia.stampy.server.mina.subscription;

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
import asia.stampy.server.listener.subscription.AbstractAcknowledgementListenerAndInterceptor;
import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;
import asia.stampy.server.mina.ServerMinaMessageGateway;

/**
 * This class assists in the publication of {@link StompMessageType#MESSAGE}
 * messages for a subscription. If confirmation of the publication is requested
 * a timer is created to await receipt of the confirmation, and the appropriate
 * methods of the {@link StampyAcknowledgementHandler} implementation are
 * invoked.
 */
@Resource
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class MinaAcknowledgementListenerAndInterceptor extends
    AbstractAcknowledgementListenerAndInterceptor<ServerMinaMessageGateway> {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  protected void ensureCleanup() {
    getGateway().addServiceListener(new MinaServiceAdapter() {

      @Override
      public void sessionDestroyed(IoSession session) throws Exception {
        HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
        if (messages.containsKey(hostPort)) {
          log.debug("{} session terminated, cleaning up message interceptor", hostPort);
          messages.remove(hostPort);
        }
      }
    });
  }

}
