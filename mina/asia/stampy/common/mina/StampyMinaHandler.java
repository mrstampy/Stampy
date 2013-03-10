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
package asia.stampy.common.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.DefaultUnparseableMessageHandler;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.MessageListenerHaltException;
import asia.stampy.common.gateway.StampyHandlerHelper;
import asia.stampy.common.gateway.UnparseableMessageHandler;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.raw.StampyRawStringHandler;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;

/**
 * This class is an abstract implementation of a MINA IoHandler for the receipt
 * of STOMP messages. It uses a MINA prefixed message payload to determine
 * message start and end, and as such violates the STOMP 1.2 specification.
 * Subclasses must only be used in an all-Stampy environment.<br>
 * <br>
 * As the MINA framework performs a much better job at parsing messages of which
 * it understands, subclasses of this class are recommended to be used in an
 * all-Stampy environment.<br>
 * <br>
 * Subclasses are singletons, wire in appropriately.
 * 
 * @see StampyRawStringHandler
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public abstract class StampyMinaHandler extends IoHandlerAdapter {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private StompMessageParser parser = new StompMessageParser();

  private StampyHeartbeatContainer heartbeatContainer;

  private AbstractStampyMessageGateway gateway;

  private static final String ILLEGAL_ACCESS_ATTEMPT = "Illegal access attempt";

  private Executor executor = Executors.newSingleThreadExecutor();

  /** <i>The default encoding for STOMP is UTF-8</i>. */
  public static Charset CHARSET = Charset.forName("UTF-8");

  private UnparseableMessageHandler unparseableMessageHandler = new DefaultUnparseableMessageHandler();

  protected StampyHandlerHelper helper = new StampyHandlerHelper();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache
   * .mina.core.session.IoSession, java.lang.Object)
   */
  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    final HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
    log.debug("Received raw message {} from {}", message, hostPort);

    helper.resetHeartbeat(hostPort);

    if (!helper.isValidObject(message)) {
      log.error("Object {} is not a valid STOMP message, closing connection {}", message, hostPort);
      illegalAccess(session);
      return;
    }

    final String msg = (String) message;

    if (helper.isHeartbeat(msg)) {
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
   * Returns a PrefixedStringCodecFactory allowing messages of maxMessageSize.
   * 
   * @param maxMessageSize
   *          the max message size
   * @return the factory
   */
  public ProtocolCodecFactory getFactory(int maxMessageSize) {
    PrefixedStringCodecFactory factory = new PrefixedStringCodecFactory(CHARSET);
    factory.setDecoderMaxDataLength(maxMessageSize);
    factory.setEncoderMaxDataLength(maxMessageSize);

    return factory;
  }

  /**
   * Once simple validation has been performed on the received message a
   * Runnable is executed by a single thread executor. This pulls the messages
   * off the thread MINA uses and ensures the messages are processed in the
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

      getGateway().notifyMessageListeners(sm, hostPort);
    } catch (UnparseableException e) {
      helper.handleUnparseableMessage(hostPort, msg, e);
    } catch (MessageListenerHaltException e) {
      // halting
    } catch (Exception e) {
      helper.handleUnexpectedError(hostPort, msg, sm, e);
    }
  }

  /**
   * Writes a context-less error to the session and terminates the session.
   * 
   * @param session
   *          the session
   */
  protected void illegalAccess(IoSession session) {
    session.write(ILLEGAL_ACCESS_ATTEMPT);
    session.close(false);
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
    helper.setParser(parser);
  }

  /**
   * Gets the heartbeat container.
   * 
   * @return the heartbeat container
   */
  public StampyHeartbeatContainer getHeartbeatContainer() {
    return heartbeatContainer;
  }

  /**
   * Sets the heartbeat container.
   * 
   * @param heartbeatContainer
   *          the new heartbeat container
   */
  public void setHeartbeatContainer(StampyHeartbeatContainer heartbeatContainer) {
    this.heartbeatContainer = heartbeatContainer;
    helper.setHeartbeatContainer(heartbeatContainer);
  }

  /**
   * Gets the message gateway.
   * 
   * @return the message gateway
   */
  public AbstractStampyMessageGateway getGateway() {
    return gateway;
  }

  /**
   * Sets the message gateway.
   * 
   * @param gateway
   *          the new message gateway
   */
  public void setGateway(AbstractStampyMessageGateway gateway) {
    this.gateway = gateway;
    helper.setGateway(gateway);
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

  /**
   * Returns the {@link UnparseableMessageHandler}, defaults to
   * {@link DefaultUnparseableMessageHandler}.
   * 
   * @return the unparseable message handler
   * 
   */
  public UnparseableMessageHandler getUnparseableMessageHandler() {
    return unparseableMessageHandler;
  }

  /**
   * Inject the appropriate {@link UnparseableMessageHandler} on system startup.
   * 
   * @param unparseableMessageHandler
   *          the new unparseable message handler
   */
  public void setUnparseableMessageHandler(UnparseableMessageHandler unparseableMessageHandler) {
    this.unparseableMessageHandler = unparseableMessageHandler;
    helper.setUnparseableMessageHandler(unparseableMessageHandler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache
   * .mina.core.session.IoSession, java.lang.Throwable)
   */
  public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());

    log.error("Unexpected exception for {}", hostPort, cause);
  }
}
