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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import asia.stampy.common.StampyLibrary;

/**
 * The Interface Remoteable.
 */
@StampyLibrary(libraryName = "stampy-examples")
public interface Remoteable extends Serializable {

  /**
   * Execute. Execute any bit of code on the server a client sends in a STOMP
   * SEND message. DO NOT do anything like this in a public facing server unless
   * you know what you are doing. Even then, don't do it.
   * 
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  boolean execute() throws Exception;

  /**
   * Sets the properties.
   * 
   * @param properties
   *          the properties
   * @throws MissingPropertyException
   *           the missing propery exception
   */
  void setProperties(Map<String, List<String>> properties) throws MissingPropertyException;
}
