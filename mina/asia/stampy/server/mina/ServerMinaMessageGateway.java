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
package asia.stampy.server.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;

/**
 * This class is the reference implementation of a Stampy <a
 * href="https://mina.apache.org">MINA</a> server gateway.
 */
@Resource
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class ServerMinaMessageGateway extends AbstractStampyMinaMessageGateway {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private NioSocketAcceptor acceptor = new NioSocketAcceptor();

  private void init() {
    log.trace("Initializing Stampy MINA acceptor");

    serviceAdapter.setGateway(this);
    serviceAdapter.setAutoShutdown(isAutoShutdown());

    acceptor.setReuseAddress(true);
    acceptor.setCloseOnDeactivation(true);

    acceptor.setHandler(getHandler());

    acceptor.addListener(serviceAdapter);

    DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

    MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
    chain.addLast("mdc", mdcInjectionFilter);
    chain.addLast("codec", new ProtocolCodecFilter(getHandler().getFactory(getMaxMessageSize())));
    log.trace("Acceptor initialized");
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.AbstractStampyMessageGateway#connect()
   */
  @Override
  public void connect() throws Exception {
    log.trace("connect() invoked");

    if (acceptor != null && acceptor.isActive()) {
      log.warn("connect invoked when already connected");
      return;
    }

    if (acceptor == null || acceptor.isDisposed()) {
      acceptor = new NioSocketAcceptor();
      addServiceListeners();
    }

    if (!acceptor.isActive()) init();

    acceptor.bind(new InetSocketAddress(getPort()));
    log.info("connect() invoked, bound to port {}", getPort());
  }

  private void addServiceListeners() {
    for (IoServiceListener l : getServiceListeners()) {
      acceptor.addListener(l);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.AbstractStampyMessageGateway#isConnected(asia.stampy
   * .common.HostPort)
   */
  @Override
  public boolean isConnected(HostPort hostPort) {
    return serviceAdapter.hasSession(hostPort) && acceptor.isActive();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.AbstractStampyMessageGateway#sendMessage(java.lang.String
   * , asia.stampy.common.HostPort)
   */
  @Override
  public void sendMessage(String message, HostPort hostPort) throws InterceptException {
    if (!isConnected(hostPort)) {
      log.warn("Attempting to send message {} to {} when the acceptor is not active", message, hostPort);
      throw new IllegalStateException("The acceptor is not active, cannot send message");
    }

    interceptOutgoingMessage(message);

    getHandler().getHeartbeatContainer().reset(hostPort);
    serviceAdapter.sendMessage(message, hostPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.AbstractStampyMessageGateway#broadcastMessage(java.lang
   * .String)
   */
  @Override
  public void broadcastMessage(String message) throws InterceptException {
    if (!acceptor.isActive()) {
      log.warn("Attempting to broadcast {} when the acceptor is not active", message);
      throw new IllegalStateException("The acceptor is not active, cannot send message");
    }

    interceptOutgoingMessage(message);

    for (HostPort hostPort : serviceAdapter.getHostPorts()) {
      getHandler().getHeartbeatContainer().reset(hostPort);
    }

    acceptor.broadcast(message);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.AbstractStampyMessageGateway#closeConnection(asia.stampy
   * .common.HostPort)
   */
  @Override
  public void closeConnection(HostPort hostPort) {
    if (!serviceAdapter.hasSession(hostPort)) return;
    log.info("closeConnection() invoked, closing session for {}", hostPort);

    IoSession session = serviceAdapter.getSession(hostPort);
    CloseFuture cf = session.close(false);
    cf.awaitUninterruptibly();
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.AbstractStampyMessageGateway#shutdown()
   */
  @Override
  public void shutdown() throws Exception {
    log.info("shutdown() invoked, disposing the acceptor");
    serviceAdapter.closeAllSessions();
    acceptor.dispose(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#addServiceListener
   * (org.apache.mina.core.service.IoServiceListener)
   */
  @Override
  protected void addServiceListenerImpl(IoServiceListener listener) {
    acceptor.addListener(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#removeServiceListener
   * (org.apache.mina.core.service.IoServiceListener)
   */
  @Override
  protected void removeServiceListenerImpl(IoServiceListener listener) {
    acceptor.removeListener(listener);
  }
}
