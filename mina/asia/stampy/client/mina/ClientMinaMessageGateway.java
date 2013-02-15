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
package asia.stampy.client.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;

/**
 * This class is the reference implementation of a Stampy <a
 * href="https://mina.apache.org">MINA</a> client gateway.
 */
@Resource
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class ClientMinaMessageGateway extends AbstractStampyMinaMessageGateway {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private NioSocketConnector connector = new NioSocketConnector();
  private String host;

  private void init() {
    serviceAdapter.setGateway(this);
    serviceAdapter.setAutoShutdown(isAutoShutdown());

    log.trace("Initializing Stampy MINA connector");

    connector.setHandler(getHandler());

    connector.addListener(serviceAdapter);

    DefaultIoFilterChainBuilder chain = connector.getFilterChain();

    MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
    chain.addLast("mdc", mdcInjectionFilter);
    chain.addLast("codec", new ProtocolCodecFilter(getHandler().getFactory(getMaxMessageSize())));

    log.trace("Connector initialized");
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.AbstractStampyMessageGateway#connect()
   */
  @Override
  public void connect() throws Exception {
    log.trace("connect() invoked");

    if (connector != null && connector.isActive()) {
      log.warn("connect invoked when already connected");
      return;
    }

    if (connector == null || connector.isDisposed()) {
      connector = new NioSocketConnector();
      addServiceListeners();
    }

    if (!connector.isActive()) init();

    ConnectFuture cf = connector.connect(new InetSocketAddress(getHost(), getPort()));

    cf.await(2000);
    if (connector.isActive()) {
      log.info("Stampy MINA ClientMinaMessageGateway connected to {}:{}", host, getPort());
    } else {
      log.error("Could not connect to {}:{}", host, getPort());
    }
  }

  private void addServiceListeners() {
    for (IoServiceListener l : getServiceListeners()) {
      connector.addListener(l);
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
    return serviceAdapter.hasSession(hostPort) && connector.isActive();
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
    serviceAdapter.closeAllSessions();
    connector.dispose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.AbstractStampyMessageGateway#shutdown()
   */
  @Override
  public void shutdown() throws Exception {
    closeConnection(null);
  }

  /**
   * Send message. Use
   * {@link ClientMinaMessageGateway#broadcastMessage(StampyMessage)} in
   * preference.
   * 
   * @param message
   *          the message
   * @param hostPort
   *          the host port
   * @throws InterceptException
   *           the intercept exception
   * 
   */
  @Override
  public void sendMessage(String message, HostPort hostPort) throws InterceptException {
    broadcastMessage(message);
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
    if (!connector.isActive()) {
      log.warn("Attempting to send message {} when the connector is not active", message);
      throw new IllegalStateException("The connector is not active, cannot send message");
    }

    interceptOutgoingMessage(message);

    for (HostPort hostPort : serviceAdapter.getHostPorts()) {
      getHandler().getHeartbeatContainer().reset(hostPort);
    }
    connector.broadcast(message);
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
    connector.addListener(listener);
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
    connector.removeListener(listener);
  }

  /**
   * Gets the host.
   * 
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the host.
   * 
   * @param host
   *          the new host
   */
  public void setHost(String host) {
    this.host = host;
  }

}
