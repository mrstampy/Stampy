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
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.StompMessageParser;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.raw.StampyRawStringHandler;
import asia.stampy.server.message.error.ErrorMessage;

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
 * @param <ASMG>
 *          the generic type
 * @see StampyRawStringHandler
 */
public abstract class StampyMinaHandler<ASMG extends AbstractStampyMinaMessageGateway> extends IoHandlerAdapter {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Queue<StampyMinaMessageListener> listeners = new ConcurrentLinkedQueue<>();

  private StompMessageParser parser = new StompMessageParser();

  private HeartbeatContainer heartbeatContainer;

  private ASMG messageGateway;

  private static final String ILLEGAL_ACCESS_ATTEMPT = "Illegal access attempt";

  private Executor executor = Executors.newSingleThreadExecutor();

  /** <i>The default encoding for STOMP is UTF-8</i>. */
  public static Charset CHARSET = Charset.forName("UTF-8");

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache
   * .mina.core.session.IoSession, java.lang.Object)
   */
  @Override
  public void messageReceived(final IoSession session, Object message) throws Exception {
    final HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
    log.debug("Received raw message {} from {}", message, hostPort);

    resetHeartbeat(hostPort);

    if (!isValidObject(message)) {
      log.error("Object {} is not a valid STOMP message, closing connection {}", message, hostPort);
      illegalAccess(session);
      return;
    }

    final String msg = (String) message;

    if (isHeartbeat(msg)) {
      log.trace("Received heartbeat");
      return;
    }

    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        asyncProcessing(session, hostPort, msg);
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
   * @param session
   *          the session
   * @param hostPort
   *          the host port
   * @param msg
   *          the msg
   */
  protected void asyncProcessing(IoSession session, HostPort hostPort, String msg) {
    try {
      StampyMessage<?> sm = getParser().parseMessage(msg);

      try {
        if (isValidMessage(sm)) {
          notifyListeners(sm, session, hostPort);
          sendResponseIfRequired(sm, session, hostPort);
        }
      } catch (Exception e) {
        errorHandle(sm, e, session, hostPort);
      }
    } catch (Exception e) {
      log.error("Unexpected exception processing message " + msg, e);
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
   * Send response if required. Blank implementation.
   * 
   * @param sm
   *          the sm
   * @param session
   *          the session
   * @param hostPort
   *          the host port
   */
  protected void sendResponseIfRequired(StampyMessage<?> sm, IoSession session, HostPort hostPort) {
    // Blank by dflt
  }

  /**
   * Error handle. Logs the error.
   * 
   * @param message
   *          the message
   * @param e
   *          the e
   * @param session
   *          the session
   * @param hostPort
   *          the host port
   * @throws Exception
   *           the exception
   */
  protected void errorHandle(StampyMessage<?> message, Exception e, IoSession session, HostPort hostPort)
      throws Exception {
    log.error("Unexpected exception", e);
  }

  /**
   * Error handle. Logs the error.
   * 
   * @param e
   *          the e
   * @param session
   *          the session
   * @param hostPort
   *          the host port
   * @throws Exception
   *           the exception
   */
  protected void errorHandle(Exception e, IoSession session, HostPort hostPort) throws Exception {
    ErrorMessage message = new ErrorMessage("n/a");
    message.getHeader().setMessageHeader(e.getMessage());
    getMessageGateway().sendMessage(message.toStompMessage(true), hostPort);
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
   * Checks if is valid message for a client or a server. Implementations should
   * ensure that the {@link StompMessageType} is appropriate.
   * 
   * @param message
   *          the message
   * @return true, if is valid message
   */
  protected abstract boolean isValidMessage(StampyMessage<?> message);

  /**
   * Notify listeners of received {@link StampyMessage}s.
   * 
   * @param sm
   *          the sm
   * @param session
   *          the session
   * @param hostPort
   *          the host port
   * @throws Exception
   *           the exception
   */
  protected void notifyListeners(StampyMessage<?> sm, IoSession session, HostPort hostPort) throws Exception {
    for (StampyMinaMessageListener listener : listeners) {
      if (isForType(listener.getMessageTypes(), sm.getMessageType()) && listener.isForMessage(sm)) {
        log.debug("Evaluating message {} with listener {}", sm, listener);
        listener.messageReceived(sm, session, hostPort);
      }
    }
  }

  private boolean isForType(StompMessageType[] messageTypes, StompMessageType messageType) {
    for (StompMessageType type : messageTypes) {
      if (type.equals(messageType)) return true;
    }

    return false;
  }

  /**
   * Adds the message listener.
   * 
   * @param listener
   *          the listener
   */
  public void addMessageListener(StampyMinaMessageListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes the message listener.
   * 
   * @param listener
   *          the listener
   */
  public void removeMessageListener(StampyMinaMessageListener listener) {
    listeners.remove(listener);
  }

  /**
   * Clear message listeners.
   */
  public void clearMessageListeners() {
    listeners.clear();
  }

  /**
   * Sets the listeners.
   * 
   * @param listeners
   *          the new listeners
   */
  public void setListeners(Collection<StampyMinaMessageListener> listeners) {
    this.listeners.addAll(listeners);
  }

  public int messageListenerSize() {
    return listeners.size();
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
   * Gets the message gateway.
   * 
   * @return the message gateway
   */
  public ASMG getMessageGateway() {
    return messageGateway;
  }

  /**
   * Sets the message gateway.
   * 
   * @param messageGateway
   *          the new message gateway
   */
  public void setMessageGateway(ASMG messageGateway) {
    this.messageGateway = messageGateway;
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
