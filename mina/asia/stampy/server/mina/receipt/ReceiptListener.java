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
package asia.stampy.server.mina.receipt;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.DISCONNECT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.ClientMessageHeader;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.receipt.ReceiptMessage;
import asia.stampy.server.mina.ServerMinaMessageGateway;

/**
 * This class generates a RECEIPT message for client messages with the receipt header populated.
 */
@Resource
public class ReceiptListener implements StampyMinaMessageListener {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static StompMessageType[] TYPES = { ABORT, ACK, BEGIN, COMMIT, DISCONNECT, NACK, SEND, SUBSCRIBE, UNSUBSCRIBE };

  private ServerMinaMessageGateway gateway;

  /* (non-Javadoc)
   * @see asia.stampy.common.mina.StampyMinaMessageListener#getMessageTypes()
   */
  @Override
  public StompMessageType[] getMessageTypes() {
    return TYPES;
  }

  /* (non-Javadoc)
   * @see asia.stampy.common.mina.StampyMinaMessageListener#isForMessage(asia.stampy.common.message.StampyMessage)
   */
  @Override
  public boolean isForMessage(StampyMessage<?> message) {
    String receipt = getReceipt(message);
    return StringUtils.isNotEmpty(receipt);
  }

  /* (non-Javadoc)
   * @see asia.stampy.common.mina.StampyMinaMessageListener#messageReceived(asia.stampy.common.message.StampyMessage, org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
   */
  @Override
  public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
    ReceiptMessage msg = new ReceiptMessage(getReceipt(message));

    getGateway().sendMessage(msg, hostPort);
    log.debug("Sent RECEIPT message to {}", hostPort);
  }

  private String getReceipt(StampyMessage<?> message) {
    return message.getHeader().getHeaderValue(ClientMessageHeader.RECEIPT);
  }

  /**
   * Gets the gateway.
   *
   * @return the gateway
   */
  public ServerMinaMessageGateway getGateway() {
    return gateway;
  }

  /**
   * Inject the {@link ServerMinaMessageGateway} on system startup.
   *
   * @param gateway the new gateway
   */
  public void setGateway(ServerMinaMessageGateway gateway) {
    this.gateway = gateway;
  }

}
