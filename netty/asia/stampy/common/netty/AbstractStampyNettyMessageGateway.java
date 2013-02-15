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
package asia.stampy.common.netty;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.interceptor.InterceptException;

/**
 * The Class AbstractStampyNettyMessageGateway.
 */
@StampyLibrary(libraryName = "stampy-NETTY-client-server-RI")
public abstract class AbstractStampyNettyMessageGateway extends AbstractStampyMessageGateway {

  /** <i>The default encoding for STOMP is UTF-8</i>. */
  public static Charset CHARSET = Charset.forName("UTF-8");

  private StampyNettyChannelHandler handler;

  private List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();

  protected void initializeChannel(final Bootstrap bootstrap) {
    ChannelPipelineFactory factory = new ChannelPipelineFactory() {

      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        setupChannelPipeline(pipeline, getMaxMessageSize());
        return pipeline;
      }
    };

    bootstrap.setPipelineFactory(factory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#broadcastMessage
   * (java.lang.String)
   */
  @Override
  public void broadcastMessage(String stompMessage) throws InterceptException {
    getHandler().broadcastMessage(stompMessage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#sendMessage(java
   * .lang.String, asia.stampy.common.gateway.HostPort)
   */
  @Override
  public void sendMessage(String stompMessage, HostPort hostPort) throws InterceptException {
    getHandler().sendMessage(stompMessage, hostPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#isConnected(asia
   * .stampy.common.gateway.HostPort)
   */
  @Override
  public boolean isConnected(HostPort hostPort) {
    return getHandler().isConnected(hostPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#getConnectedHostPorts
   * ()
   */
  @Override
  public Set<HostPort> getConnectedHostPorts() {
    return getHandler().getConnectedHostPorts();
  }

  /**
   * Gets the stampy channel handler.
   * 
   * @return the stampy channel handler
   */
  public StampyNettyChannelHandler getHandler() {
    return handler;
  }

  /**
   * Sets the stampy channel handler.
   * 
   * @param channelHandler
   *          the new stampy channel handler
   */
  public void setHandler(StampyNettyChannelHandler channelHandler) {
    this.handler = channelHandler;
  }

  /**
   * Adds the Channel Handler for inclusion in the created Channel. Note that on
   * the server the handler will be shared across all connections, and as such
   * must be able to act as a singleton ie. no {@link FrameDecoder}s here.
   * 
   * @param handler
   *          the handler
   */
  public void addHandler(ChannelHandler handler) {
    handlers.add(handler);
  }

  /**
   * Removes the handler.
   * 
   * @param handler
   *          the handler
   */
  public void removeHandler(ChannelHandler handler) {
    handlers.remove(handler);
  }

  /**
   * Setup channel pipeline.
   * 
   * @param pipeline
   *          the pipeline
   * @param maxLength
   *          the max length
   */
  protected void setupChannelPipeline(ChannelPipeline pipeline, int maxLength) {
    addHandlers(pipeline);

    StringEncoder encoder = new StringEncoder(CHARSET);
    StringDecoder decoder = new StringDecoder(CHARSET);

    StompBasedFrameDecoder stomp = new StompBasedFrameDecoder(maxLength);

    pipeline.addLast("stompDecoder", stomp);
    pipeline.addLast("stringDecoder", decoder);
    pipeline.addLast("stringEncoder", encoder);
    pipeline.addLast("stampyChannelHandler", getHandler());
  }

  /*
   * Adds the handlers.
   * 
   * @param pipeline the pipeline
   */
  private void addHandlers(ChannelPipeline pipeline) {
    for (ChannelHandler handler : handlers) {
      pipeline.addLast(handler.toString(), handler);
    }
  }

}
