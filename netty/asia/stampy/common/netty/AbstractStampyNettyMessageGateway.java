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

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.interceptor.InterceptException;

/**
 * The Class AbstractStampyNettyMessageGateway.
 */
public abstract class AbstractStampyNettyMessageGateway extends AbstractStampyMessageGateway {

  /** <i>The default encoding for STOMP is UTF-8</i>. */
  public static Charset CHARSET = Charset.forName("UTF-8");

  private StampyNettyChannelHandler stampyChannelHandler;

  private List<ChannelHandler> handlers = new ArrayList<>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.gateway.AbstractStampyMessageGateway#broadcastMessage
   * (java.lang.String)
   */
  @Override
  public void broadcastMessage(String stompMessage) throws InterceptException {
    getStampyChannelHandler().broadcastMessage(stompMessage);
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
    getStampyChannelHandler().sendMessage(stompMessage, hostPort);
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
    return getStampyChannelHandler().isConnected(hostPort);
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
    return getStampyChannelHandler().getConnectedHostPorts();
  }

  /**
   * Gets the stampy channel handler.
   * 
   * @return the stampy channel handler
   */
  public StampyNettyChannelHandler getStampyChannelHandler() {
    return stampyChannelHandler;
  }

  /**
   * Sets the stampy channel handler.
   * 
   * @param channelHandler
   *          the new stampy channel handler
   */
  public void setStampyChannelHandler(StampyNettyChannelHandler channelHandler) {
    this.stampyChannelHandler = channelHandler;
  }

  /**
   * Adds the handler.
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
  public void setupChannelPipeline(ChannelPipeline pipeline, int maxLength) {
    addHandlers(pipeline);

    StringEncoder encoder = new StringEncoder(CHARSET);
    StringDecoder decoder = new StringDecoder(CHARSET);

    DelimiterBasedFrameDecoder delimiter = new DelimiterBasedFrameDecoder(maxLength, Delimiters.nulDelimiter());

    pipeline.addLast("frameDecoder", delimiter);
    pipeline.addLast("stringDecoder", decoder);
    pipeline.addLast("stringEncoder", encoder);
    pipeline.addLast("stampyChannelHandler", getStampyChannelHandler());
  }

  /*
   * Adds the handlers.
   * 
   * @param pipeline
   *          the pipeline
   */
  private void addHandlers(ChannelPipeline pipeline) {
    for (ChannelHandler handler : handlers) {
      pipeline.addLast(handler.toString(), handler);
    }
  }

}
