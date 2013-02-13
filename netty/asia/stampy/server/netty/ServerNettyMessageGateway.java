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
package asia.stampy.server.netty;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.netty.AbstractStampyNettyMessageGateway;

/**
 * The Class ServerNettyMessageGateway.
 */
@Resource
public class ServerNettyMessageGateway extends AbstractStampyNettyMessageGateway {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool());
  private int maxMessageSize = Integer.MAX_VALUE;

  private Channel server;

  private int port;

  private ServerBootstrap init() {
    ServerBootstrap bootstrap = new ServerBootstrap(factory);
    ChannelPipeline pipeline = new DefaultChannelPipeline();
    addHandlers(pipeline);
    getStampyChannelHandler().setupChannelPipeline(pipeline, getMaxMessageSize());
    bootstrap.setPipeline(pipeline);
    return bootstrap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#closeConnection
   * (asia.stampy.common.gateway.HostPort)
   */
  @Override
  public void closeConnection(HostPort hostPort) {
    getStampyChannelHandler().close(hostPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.gateway.AbstractStampyMessageGateway#connect()
   */
  @Override
  public void connect() throws Exception {
    if (server == null) {
      ServerBootstrap bootstrap = init();
      server = bootstrap.bind(new InetSocketAddress(getPort()));
      log.info("Bound to {}", getPort());
    } else if (server.isConnected()) {
      log.warn("Already connected");
    } else {
      log.error("Acceptor in unrecognized state: isBound {}, isConnected {}, ", server.isBound(), server.isConnected());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.gateway.AbstractStampyMessageGateway#shutdown()
   */
  @Override
  public void shutdown() throws Exception {
    if (server == null || !server.isConnected()) return;
    ChannelFuture cf = server.close();
    cf.awaitUninterruptibly();
    server = null;
    log.info("Server has been shut down");
  }

  /**
   * Gets the max message size.
   * 
   * @return the max message size
   */
  public int getMaxMessageSize() {
    return maxMessageSize;
  }

  /**
   * Sets the max message size.
   * 
   * @param maxMessageSize
   *          the new max message size
   */
  public void setMaxMessageSize(int maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  /**
   * Gets the port.
   * 
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port.
   * 
   * @param port
   *          the new port
   */
  public void setPort(int port) {
    this.port = port;
  }

}
