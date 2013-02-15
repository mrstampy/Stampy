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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.service.IoServiceListener;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;

/**
 * Defines the <a href="https://mina.apache.org">MINA</a>-specific methods for
 * the implementation of clients and servers.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public abstract class AbstractStampyMinaMessageGateway extends AbstractStampyMessageGateway {

  private List<IoServiceListener> serviceListeners = new ArrayList<IoServiceListener>();
  protected StampyServiceAdapter serviceAdapter = new StampyServiceAdapter();
  private StampyMinaHandler handler;

  /**
   * Adds the MINA service listener to the underlying connector or acceptor.
   * 
   * @param listener
   *          the listener
   */
  public void addServiceListener(IoServiceListener listener) {
    serviceListeners.add(listener);
    addServiceListenerImpl(listener);
  }

  protected abstract void addServiceListenerImpl(IoServiceListener listener);

  /**
   * Removes the MINA service listener.
   * 
   * @param listener
   *          the listener
   */
  public void removeServiceListener(IoServiceListener listener) {
    serviceListeners.remove(listener);
    removeServiceListenerImpl(listener);
  }

  public List<IoServiceListener> getServiceListeners() {
    return serviceListeners;
  }

  protected abstract void removeServiceListenerImpl(IoServiceListener listener);

  /**
   * Gets the handler.
   * 
   * @return the handler
   */
  public StampyMinaHandler getHandler() {
    return handler;
  }

  /**
   * Sets the handler.
   * 
   * @param handler
   *          the new handler
   */
  public void setHandler(StampyMinaHandler handler) {
    this.handler = handler;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.common.AbstractStampyMessageGateway#getConnectedHostPorts()
   */
  @Override
  public Set<HostPort> getConnectedHostPorts() {
    return serviceAdapter.getHostPorts();
  }

}
