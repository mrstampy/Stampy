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
package asia.stampy.server.mina.subscription;

/**
 * The Interface StampyAcknowledgementHandler.
 */
public interface StampyAcknowledgementHandler {

  /**
   * Ack received.
   * 
   * @param id
   *          the id
   * @param receipt
   *          the receipt
   * @param transaction
   *          the transaction
   * @throws Exception
   *           the exception
   */
  void ackReceived(String id, String receipt, String transaction) throws Exception;

  /**
   * Nack received.
   * 
   * @param id
   *          the id
   * @param receipt
   *          the receipt
   * @param transaction
   *          the transaction
   * @throws Exception
   *           the exception
   */
  void nackReceived(String id, String receipt, String transaction) throws Exception;

  /**
   * No acknowledgement received.
   * 
   * @param id
   *          the id
   */
  void noAcknowledgementReceived(String id);

}
