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

import java.util.Collection;

import org.apache.mina.core.service.IoServiceListener;

import asia.stampy.common.AbstractStampyMessageGateway;

/**
 * Defines the <a href="https://mina.apache.org">MINA</a>-specific methods for
 * the implementation of clients and servers.
 */
public abstract class AbstractStampyMinaMessageGateway extends AbstractStampyMessageGateway {

  /**
   * Adds the message listener.
   * 
   * @param listener
   *          the listener
   */
  public abstract void addMessageListener(StampyMinaMessageListener listener);

  /**
   * Removes the message listener.
   * 
   * @param listener
   *          the listener
   */
  public abstract void removeMessageListener(StampyMinaMessageListener listener);

  /**
   * Clear message listeners.
   */
  public abstract void clearMessageListeners();

  /**
   * Sets the listeners. Specified for DI frameworks; programmatic usage should
   * invoke the
   * {@link AbstractStampyMinaMessageGateway#addMessageListener(StampyMinaMessageListener)}
   * method to specify {@link StampyMinaMessageListener}s.
   * 
   * @param listeners
   *          the new listeners
   * 
   */
  public abstract void setListeners(Collection<StampyMinaMessageListener> listeners);

  /**
   * Adds the MINA service listener to the underlying connector or acceptor.
   * 
   * @param listener
   *          the listener
   */
  public abstract void addServiceListener(IoServiceListener listener);

  /**
   * Removes the MINA service listener.
   * 
   * @param listener
   *          the listener
   */
  public abstract void removeServiceListener(IoServiceListener listener);

}
