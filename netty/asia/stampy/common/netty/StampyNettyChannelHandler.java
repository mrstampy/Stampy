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

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.ClientMessageHeader;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.DefaultUnparseableMessageHandler;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.MessageListenerHaltException;
import asia.stampy.common.gateway.UnparseableMessageHandler;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;
import asia.stampy.server.message.error.ErrorMessage;

/**
 * The Class StampyNettyChannelHandler.
 */
public abstract class StampyNettyChannelHandler extends SimpleChannelUpstreamHandler {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private StompMessageParser parser = new StompMessageParser();

  private HeartbeatContainer heartbeatContainer;

  private AbstractStampyMessageGateway gateway;

  private static final String ILLEGAL_ACCESS_ATTEMPT = "Illegal access attempt";

  private Executor executor = Executors.newSingleThreadExecutor();

  /** <i>The default encoding for STOMP is UTF-8</i>. */
  public static Charset CHARSET = Charset.forName("UTF-8");

  private UnparseableMessageHandler unparseableMessageHandler = new DefaultUnparseableMessageHandler();

  private Map<HostPort, Channel> sessions = new ConcurrentHashMap<>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org
   * .jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.MessageEvent)
   */
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    final HostPort hostPort = createHostPort(ctx);
    log.debug("Received raw message {} from {}", e.getMessage(), hostPort);

    resetHeartbeat(hostPort);

    if (!isValidObject(e.getMessage())) {
      log.error("Object {} is not a valid STOMP message, closing connection {}", e.getMessage(), hostPort);
      illegalAccess(ctx);
      return;
    }

    final String msg = (String) e.getMessage();

    if (isHeartbeat(msg)) {
      log.trace("Received heartbeat");
      return;
    }

    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        asyncProcessing(hostPort, msg);
      }
    };

    getExecutor().execute(runnable);
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
    StringEncoder encoder = new StringEncoder(CHARSET);
    StringDecoder decoder = new StringDecoder(CHARSET);

    DelimiterBasedFrameDecoder delimiter = new DelimiterBasedFrameDecoder(maxLength, Delimiters.nulDelimiter());

    pipeline.addLast("frameDecoder", delimiter);
    pipeline.addLast("stringDecoder", decoder);
    pipeline.addLast("stringEncoder", encoder);
    pipeline.addLast("stampyChannelHandler", this);
  }

  /**
   * Creates the host port.
   * 
   * @param ctx
   *          the ctx
   * @return the host port
   */
  protected HostPort createHostPort(ChannelHandlerContext ctx) {
    return new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
  }

  /**
   * Invoked when a {@link Channel} is open, bound to a local address, and
   * connected to a remote address. <br/>
   * 
   * <strong>Be aware that this event is fired from within the Boss-Thread so
   * you should not execute any heavy operation in there as it will block the
   * dispatching to other workers!</strong>
   * 
   * @param ctx
   *          the ctx
   * @param e
   *          the e
   * @throws Exception
   *           the exception
   */
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    HostPort hostPort = createHostPort(ctx);
    sessions.put(hostPort, ctx.getChannel());
    ctx.sendUpstream(e);
  }

  /**
   * Invoked when a {@link Channel} was disconnected from its remote peer.
   * 
   * @param ctx
   *          the ctx
   * @param e
   *          the e
   * @throws Exception
   *           the exception
   */
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    HostPort hostPort = createHostPort(ctx);
    sessions.remove(hostPort);
    ctx.sendUpstream(e);
  }

  /**
   * Gets the connected host ports.
   * 
   * @return the connected host ports
   */
  public Set<HostPort> getConnectedHostPorts() {
    return Collections.unmodifiableSet(sessions.keySet());
  }

  /**
   * Checks if is connected.
   * 
   * @param hostPort
   *          the host port
   * @return true, if is connected
   */
  public boolean isConnected(HostPort hostPort) {
    return sessions.containsKey(hostPort);
  }

  /**
   * Broadcast message.
   * 
   * @param message
   *          the message
   */
  public void broadcastMessage(String message) {
    for (HostPort hostPort : sessions.keySet()) {
      sendMessage(message, hostPort);
    }
  }

  /**
   * Send message.
   * 
   * @param message
   *          the message
   * @param hostPort
   *          the host port
   */
  public void sendMessage(String message, HostPort hostPort) {
    if (!isConnected(hostPort)) {
      log.error("{} is not connected, cannot send message {}", hostPort, message);
      return;
    }

    resetHeartbeat(hostPort);

    Channel channel = sessions.get(hostPort);
    channel.write(message);
  }

  /**
   * Close.
   * 
   * @param hostPort
   *          the host port
   */
  public void close(HostPort hostPort) {
    if (!isConnected(hostPort)) {
      log.warn("{} is already closed");
      return;
    }

    Channel channel = sessions.get(hostPort);
    ChannelFuture cf = channel.close();
    cf.awaitUninterruptibly();
    log.info("Session for {} has been closed", hostPort);
  }

  /**
   * Once simple validation has been performed on the received message a
   * Runnable is executed by a single thread executor. This pulls the messages
   * off the thread NETTY uses and ensures the messages are processed in the
   * order they are received.
   * 
   * @param hostPort
   *          the host port
   * @param msg
   *          the msg
   */
  protected void asyncProcessing(HostPort hostPort, String msg) {
    StampyMessage<?> sm = null;
    try {
      sm = getParser().parseMessage(msg);

      if (isValidMessage(sm)) getGateway().notifyMessageListeners(sm, hostPort);
    } catch (UnparseableException e) {
      handleUnparseableMessage(hostPort, msg, e);
    } catch (MessageListenerHaltException e) {
      // halting
    } catch (Exception e) {
      handleUnexpectedError(hostPort, msg, sm, e);
    }
  }

  /**
   * Handle unexpected error.
   * 
   * @param hostPort
   *          the host port
   * @param msg
   *          the msg
   * @param sm
   *          the sm
   * @param e
   *          the e
   */
  protected void handleUnexpectedError(HostPort hostPort, String msg, StampyMessage<?> sm, Exception e) {
    try {
      if (sm == null) {
        errorHandle(e, hostPort);
      } else {
        errorHandle(sm, e, hostPort);
      }
    } catch (Exception e1) {
      log.error("Unexpected exception sending error message for " + hostPort, e1);
    }
  }

  /**
   * Handle unparseable message.
   * 
   * @param hostPort
   *          the host port
   * @param msg
   *          the msg
   * @param e
   *          the e
   */
  protected void handleUnparseableMessage(HostPort hostPort, String msg, UnparseableException e) {
    log.debug("Unparseable message, delegating to unparseable message handler");
    try {
      getUnparseableMessageHandler().unparseableMessage(msg, hostPort);
    } catch (Exception e1) {
      try {
        errorHandle(e1, hostPort);
      } catch (Exception e2) {
        log.error("Could not parse message " + msg + " for " + hostPort, e);
        log.error("Unexpected exception sending error message for " + hostPort, e2);
      }
    }
  }

  /**
   * Error handle. Logs the error.
   * 
   * @param message
   *          the message
   * @param e
   *          the e
   * @param hostPort
   *          the host port
   * @throws Exception
   *           the exception
   */
  protected void errorHandle(StampyMessage<?> message, Exception e, HostPort hostPort) throws Exception {
    log.error("Handling error, sending error message to " + hostPort, e);
    String receipt = message.getHeader().getHeaderValue(ClientMessageHeader.RECEIPT);
    ErrorMessage error = new ErrorMessage(StringUtils.isEmpty(receipt) ? "n/a" : receipt);
    error.getHeader().setMessageHeader("Could not execute " + message.getMessageType() + " - " + e.getMessage());
    getGateway().sendMessage(error.toStompMessage(true), hostPort);
  }

  /**
   * Error handle. Logs the error.
   * 
   * @param e
   *          the e
   * @param hostPort
   *          the host port
   * @throws Exception
   *           the exception
   */
  protected void errorHandle(Exception e, HostPort hostPort) throws Exception {
    log.error("Handling error, sending error message to " + hostPort, e);
    ErrorMessage error = new ErrorMessage("n/a");
    error.getHeader().setMessageHeader(e.getMessage());
    getGateway().sendMessage(error.toStompMessage(true), hostPort);
  }

  /**
   * Checks if is valid message for a client or a server. Implementations should
   * ensure that the {@link StompMessageType} is appropriate.
   * 
   * @param message
   *          the message
   * @return true, if is valid message
   */
  protected abstract boolean isValidMessage(StampyMessage<?> message);

  /**
   * Checks if the message is a heartbeat.
   * 
   * @param msg
   *          the msg
   * @return true, if is heartbeat
   */
  protected boolean isHeartbeat(String msg) {
    return msg.equals(PaceMaker.HB1) || msg.equals(PaceMaker.HB2);
  }

  /**
   * Illegal access.
   * 
   * @param ctx
   *          the ctx
   */
  protected void illegalAccess(ChannelHandlerContext ctx) {
    ChannelFuture cf = ctx.getChannel().write(ILLEGAL_ACCESS_ATTEMPT);
    cf.awaitUninterruptibly();
    cf = ctx.getChannel().close();
    cf.awaitUninterruptibly();
  }

  /**
   * Checks if is valid object. Must be a string.
   * 
   * @param message
   *          the message
   * @return true, if is valid object
   */
  protected boolean isValidObject(Object message) {
    return message instanceof String;
  }

  /**
   * Reset heartbeat.
   * 
   * @param hostPort
   *          the host port
   */
  protected void resetHeartbeat(HostPort hostPort) {
    getHeartbeatContainer().reset(hostPort);
  }

  /**
   * Gets the parser.
   * 
   * @return the parser
   */
  public StompMessageParser getParser() {
    return parser;
  }

  /**
   * Sets the parser.
   * 
   * @param parser
   *          the new parser
   */
  public void setParser(StompMessageParser parser) {
    this.parser = parser;
  }

  /**
   * Gets the heartbeat container.
   * 
   * @return the heartbeat container
   */
  public HeartbeatContainer getHeartbeatContainer() {
    return heartbeatContainer;
  }

  /**
   * Sets the heartbeat container.
   * 
   * @param heartbeatContainer
   *          the new heartbeat container
   */
  public void setHeartbeatContainer(HeartbeatContainer heartbeatContainer) {
    this.heartbeatContainer = heartbeatContainer;
  }

  /**
   * Gets the gateway.
   * 
   * @return the gateway
   */
  public AbstractStampyMessageGateway getGateway() {
    return gateway;
  }

  /**
   * Sets the gateway.
   * 
   * @param gateway
   *          the new gateway
   */
  public void setGateway(AbstractStampyMessageGateway gateway) {
    this.gateway = gateway;
  }

  /**
   * Gets the unparseable message handler.
   * 
   * @return the unparseable message handler
   */
  public UnparseableMessageHandler getUnparseableMessageHandler() {
    return unparseableMessageHandler;
  }

  /**
   * Sets the unparseable message handler.
   * 
   * @param unparseableMessageHandler
   *          the new unparseable message handler
   */
  public void setUnparseableMessageHandler(UnparseableMessageHandler unparseableMessageHandler) {
    this.unparseableMessageHandler = unparseableMessageHandler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org
   * .jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.ExceptionEvent)
   */
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    HostPort hostPort = createHostPort(ctx);
    log.error("Unexpected Netty exception for " + hostPort, e);
  }

  /**
   * Gets the executor.
   * 
   * @return the executor
   */
  public Executor getExecutor() {
    return executor;
  }

  /**
   * Sets the executor.
   * 
   * @param executor
   *          the new executor
   */
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

}
