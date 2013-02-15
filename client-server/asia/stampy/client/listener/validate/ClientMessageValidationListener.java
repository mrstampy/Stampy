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
package asia.stampy.client.listener.validate;

import javax.annotation.Resource;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

/**
 * Ensures that only server messages are accepted on the client. The validate()
 * method on the message is invoked.
 */
@Resource
@StampyLibrary(libraryName="stampy-client-server")
public class ClientMessageValidationListener implements StampyMessageListener {

  private static StompMessageType[] TYPES = StompMessageType.values();

  /* (non-Javadoc)
   * @see asia.stampy.common.gateway.StampyMessageListener#getMessageTypes()
   */
  @Override
  public StompMessageType[] getMessageTypes() {
    return TYPES;
  }

  /* (non-Javadoc)
   * @see asia.stampy.common.gateway.StampyMessageListener#isForMessage(asia.stampy.common.message.StampyMessage)
   */
  @Override
  public boolean isForMessage(StampyMessage<?> message) {
    return true;
  }

  /* (non-Javadoc)
   * @see asia.stampy.common.gateway.StampyMessageListener#messageReceived(asia.stampy.common.message.StampyMessage, asia.stampy.common.gateway.HostPort)
   */
  @Override
  public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
    switch (message.getMessageType()) {

    case CONNECTED:
    case ERROR:
    case MESSAGE:
    case RECEIPT:
      message.validate();
      break;
    default:
      throw new IllegalArgumentException(message.getMessageType() + " is not a valid STOMP 1.2 server message");
    }
  }

}
