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

import asia.stampy.common.HostPort;
import asia.stampy.common.StompMessageParser;
import asia.stampy.common.UnparseableException;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaHandler;

/**
 * This class uses its own message parsing to piece together STOMP messages. In
 * non-Stampy STOMP environments subclasses are to be used. While tested
 * successfully in simple cases it has not (yet) been battle-tested. Use at your
 * own risk.
 * 
 * @param <ASMG>
 *          the generic type
 */
public abstract class StampyRawStringHandler extends StampyMinaHandler {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<HostPort, String> messageParts = new ConcurrentHashMap<>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.mina.StampyMinaHandler#messageReceived(org.apache.mina
   * .core.session.IoSession, java.lang.Object)
   */
  @Override
  public void messageReceived(final IoSession session, Object message) throws Exception {
    final HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
    log.trace("Received raw message {} from {}", message, hostPort);

    resetHeartbeat(hostPort);

    if (!isValidObject(message)) {
      log.error("Object {} is not a valid STOMP message, closing connection {}", message, hostPort);
      illegalAccess(session);
      return;
    }

    final String msg = (String) message;

    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        asyncProcessing(session, hostPort, msg);
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
  protected void asyncProcessing(IoSession session, HostPort hostPort, String msg) {
    try {
      String existing = messageParts.get(hostPort);
      if (StringUtils.isEmpty(existing)) {
        if (isHeartbeat(msg)) {
          log.trace("Received heartbeat");
          return;
        } else if (isStompMessage(msg)) {
          processMessage(msg, session, hostPort);
        } else {
          log.error("Message {} is not a valid STOMP message, closing connection {}", msg, hostPort);
          illegalAccess(session);
        }
      } else {
        String concat = existing + msg;
        processMessage(concat, session, hostPort);
      }
    } catch (UnparseableException e) {
      handleUnparseableMessage(session, hostPort, msg, e);
    } catch (Exception e) {
      handleUnexpectedError(session, hostPort, msg, null, e);
    }
  }

  private void processMessage(String msg, IoSession session, HostPort hostPort) throws UnparseableException, Exception,
      IOException {
    if (isHeartbeat(msg)) {
      log.debug("Simple heartbeat received");
      return;
    }

    int length = msg.length();
    int idx = msg.indexOf(StompMessageParser.EOM);

    if (idx == length - 1) {
      log.trace("Creating StampyMessage from {}", msg);
      processStompMessage(msg, session, hostPort);
    } else if (idx > 0) {
      log.trace("Multiple messages detected, parsing {}", msg);
      processMultiMessages(msg, session, hostPort);
    } else {
      messageParts.put(hostPort, msg);
      log.trace("Message part {} stored for {}", msg, hostPort);
    }
  }

  private void processMultiMessages(String msg, IoSession session, HostPort hostPort) throws UnparseableException,
      Exception, IOException {
    int idx = msg.indexOf(StompMessageParser.EOM);
    String fullMessage = msg.substring(0, idx + 1);
    String partMessage = msg.substring(idx);
    if (partMessage.startsWith(StompMessageParser.EOM)) {
      partMessage = partMessage.substring(1);
    }

    processStompMessage(fullMessage, session, hostPort);

    processMessage(partMessage, session, hostPort);
  }

  private void processStompMessage(String msg, IoSession session, HostPort hostPort) {
    messageParts.remove(hostPort);
    StampyMessage<?> sm = null;
    try {
      sm = getParser().parseMessage(msg);
      if (isValidMessage(sm)) {
        notifyListeners(sm, session, hostPort);
      }
    } catch (Exception e) {
      handleUnexpectedError(session, hostPort, msg, sm, e);
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
