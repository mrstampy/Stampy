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
package asia.stampy.examples.system.server;

import asia.stampy.common.StampyLibrary;
import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;

/**
 * The Class SystemAcknowledgementHandler.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemAcknowledgementHandler implements StampyAcknowledgementHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#ackReceived
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void ackReceived(String id, String receipt, String transaction) throws Exception {

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#nackReceived
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void nackReceived(String id, String receipt, String transaction) throws Exception {

  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#
   * noAcknowledgementReceived(java.lang.String)
   */
  @Override
  public void noAcknowledgementReceived(String id) {
    System.out.println("No acknowledgement received for " + id);
  }

}
