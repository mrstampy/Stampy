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

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import asia.stampy.common.StampyLibrary;

/**
 * Convenience adapter to ignore unused interface methods.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class MinaServiceAdapter implements IoServiceListener {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoServiceListener#serviceActivated(org.apache
   * .mina.core.service.IoService)
   */
  @Override
  public void serviceActivated(IoService service) throws Exception {
    // blank
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoServiceListener#serviceIdle(org.apache.mina
   * .core.service.IoService, org.apache.mina.core.session.IdleStatus)
   */
  @Override
  public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
    // blank
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoServiceListener#serviceDeactivated(org.apache
   * .mina.core.service.IoService)
   */
  @Override
  public void serviceDeactivated(IoService service) throws Exception {
    // blank
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoServiceListener#sessionCreated(org.apache
   * .mina.core.session.IoSession)
   */
  @Override
  public void sessionCreated(IoSession session) throws Exception {
    // blank
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.core.service.IoServiceListener#sessionDestroyed(org.apache
   * .mina.core.session.IoSession)
   */
  @Override
  public void sessionDestroyed(IoSession session) throws Exception {
    // blank
  }

}
