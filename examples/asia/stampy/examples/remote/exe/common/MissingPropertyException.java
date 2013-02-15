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

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.message.StampyMessageHeader;

/**
 * Thrown when a property is missing from the {@link StampyMessageHeader}.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class MissingPropertyException extends Exception {
  private static final long serialVersionUID = 4900660741154749582L;

  /**
   * Instantiates a new missing propery exception.
   */
  public MissingPropertyException() {

  }

  /**
   * Instantiates a new missing propery exception.
   * 
   * @param message
   *          the message
   */
  public MissingPropertyException(String message) {
    super(message);
  }

  /**
   * Instantiates a new missing propery exception.
   * 
   * @param cause
   *          the cause
   */
  public MissingPropertyException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new missing propery exception.
   * 
   * @param message
   *          the message
   * @param cause
   *          the cause
   */
  public MissingPropertyException(String message, Throwable cause) {
    super(message, cause);
  }

}
