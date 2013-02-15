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
package asia.stampy.common.mina.raw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.MessageListenerHaltException;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaHandler;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;

/**
 * This class uses its own message parsing to piece together STOMP messages. In
 * non-Stampy STOMP environments subclasses are to be used. While tested
 * successfully in simple cases it has not (yet) been battle-tested. Use at your
 * own risk.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public abstract class StampyRawStringHandler extends StampyMinaHandler {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<HostPort, String> messageParts = new ConcurrentHashMap<HostPort, String>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.StampyMinaHandler#messageReceived(org.apache.mina
   * .core.session.IoSession, java.lang.Object)
   */
  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    final HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
    log.trace("Received raw message {} from {}", message, hostPort);

    helper.resetHeartbeat(hostPort);

    if (!helper.isValidObject(message)) {
      log.error("Object {} is not a valid STOMP message, closing connection {}", message, hostPort);
      illegalAccess(session);
      return;
    }

    final String msg = (String) message;

    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        asyncProcessing(hostPort, msg);
      }
    };

    getExecutor().execute(runnable);
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.common.mina.StampyMinaHandler#getFactory(int)
   */
  @Override
  public ProtocolCodecFactory getFactory(int maxMessageSize) {
    return new StringCodecFactory(maxMessageSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.StampyMinaHandler#asyncProcessing(org.apache.mina
   * .core.session.IoSession, asia.stampy.common.HostPort, java.lang.String)
   */
  @Override
  protected void asyncProcessing(HostPort hostPort, String msg) {
    try {
      String existing = messageParts.get(hostPort);
      if (StringUtils.isEmpty(existing)) {
        processNewMessage(hostPort, msg);
      } else {
        String concat = existing + msg;
        processMessage(concat, hostPort);
      }
    } catch (UnparseableException e) {
      helper.handleUnparseableMessage(hostPort, msg, e);
    } catch (MessageListenerHaltException e) {
      // halting
    } catch (Exception e) {
      helper.handleUnexpectedError(hostPort, msg, null, e);
    }
  }

  private void processNewMessage(HostPort hostPort, String msg) throws Exception, UnparseableException, IOException {
    if (helper.isHeartbeat(msg)) {
      log.trace("Received heartbeat");
      return;
    } else if (isStompMessage(msg)) {
      processMessage(msg, hostPort);
    } else {
      helper.handleUnparseableMessage(hostPort, msg, null);
    }
  }

  private void processMessage(String msg, HostPort hostPort) throws Exception {
    int length = msg.length();
    int idx = msg.indexOf(StompMessageParser.EOM);

    if (idx == length - 1) {
      log.trace("Creating StampyMessage from {}", msg);
      processStompMessage(msg, hostPort);
    } else if (idx > 0) {
      log.trace("Multiple messages detected, parsing {}", msg);
      processMultiMessages(msg, hostPort);
    } else {
      messageParts.put(hostPort, msg);
      log.trace("Message part {} stored for {}", msg, hostPort);
    }
  }

  private void processMultiMessages(String msg, HostPort hostPort) throws Exception {
    int idx = msg.indexOf(StompMessageParser.EOM);
    String fullMessage = msg.substring(0, idx + 1);
    String partMessage = msg.substring(idx);
    if (partMessage.startsWith(StompMessageParser.EOM)) {
      partMessage = partMessage.substring(1);
    }

    processStompMessage(fullMessage, hostPort);

    processMessage(partMessage, hostPort);
  }

  private void processStompMessage(String msg, HostPort hostPort) throws MessageListenerHaltException {
    messageParts.remove(hostPort);
    StampyMessage<?> sm = null;
    try {
      sm = getParser().parseMessage(msg);
      getGateway().notifyMessageListeners(sm, hostPort);
    } catch (MessageListenerHaltException e) {
      throw e;
    } catch (Exception e) {
      helper.handleUnexpectedError(hostPort, msg, sm, e);
    }
  }

  private boolean isStompMessage(String msg) throws Exception {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(msg));
      String stompMessageType = reader.readLine();

      StompMessageType type = StompMessageType.valueOf(stompMessageType);
      return type != null;
    } finally {
      if (reader != null) reader.close();
    }
  }

}
