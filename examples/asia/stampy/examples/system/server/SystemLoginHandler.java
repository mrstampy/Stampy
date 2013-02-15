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
import asia.stampy.server.listener.login.NotLoggedInException;
import asia.stampy.server.listener.login.StampyLoginHandler;
import asia.stampy.server.listener.login.TerminateSessionException;

/**
 * The Class SystemLoginHandler.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class SystemLoginHandler implements StampyLoginHandler {

  /** The Constant SEE_THE_SYSTEM_ADMINISTRATOR. */
  public static final String SEE_THE_SYSTEM_ADMINISTRATOR = "See the system administrator";

  /** The Constant GOOD_USER. */
  public static final String GOOD_USER = "gooduser";

  /** The Constant BAD_USER. */
  public static final String BAD_USER = "baduser";

  private int maxFailedLoginAttempts = 3;

  private int failedLoginAttempts = 0;

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.server.mina.login.StampyLoginHandler#login(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void login(String username, String password) throws NotLoggedInException, TerminateSessionException {
    if (GOOD_USER.equals(username)) return;

    failedLoginAttempts++;

    if (failedLoginAttempts >= getMaxFailedLoginAttempts()) {
      throw new TerminateSessionException(SEE_THE_SYSTEM_ADMINISTRATOR);
    }

    throw new NotLoggedInException("Username " + username + " cannot be logged in");
  }

  /**
   * Gets the max failed login attempts.
   * 
   * @return the max failed login attempts
   */
  public int getMaxFailedLoginAttempts() {
    return maxFailedLoginAttempts;
  }

  /**
   * Sets the max failed login attempts.
   * 
   * @param maxFailedLoginAttempts
   *          the new max failed login attempts
   */
  public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
    this.maxFailedLoginAttempts = maxFailedLoginAttempts;
  }

}
