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
package asia.stampy.examples.remote.exe.common;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;

/**
 * Executes a {@link Remoteable} object and if a receipt has been specified,
 * returns a RECEIPT message. An ERROR message is returned to the client should
 * anything unexpected occur.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class RemoteExecutor {

  private AbstractStampyMessageGateway gateway;

  /**
   * Process stomp message.
   * 
   * @param message
   *          the message
   * @param hostPort
   *          the host port
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean processStompMessage(SendMessage message, HostPort hostPort) throws Exception {
    Remoteable remoteable = message.getBody();

    remoteable.setProperties(message.getHeader().getHeaders());

    return remoteable.execute();
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
   * @param messageSender
   *          the new gateway
   */
  public void setGateway(AbstractStampyMessageGateway messageSender) {
    this.gateway = messageSender;
  }
}
