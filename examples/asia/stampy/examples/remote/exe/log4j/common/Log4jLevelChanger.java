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
package asia.stampy.examples.remote.exe.log4j.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import asia.stampy.common.StampyLibrary;
import asia.stampy.examples.remote.exe.common.MissingPropertyException;
import asia.stampy.examples.remote.exe.common.Remoteable;

/**
 * The Class Log4jLevelChanger.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class Log4jLevelChanger implements Remoteable {
  private static final long serialVersionUID = -8963468052541849253L;

  /** The Constant LEVEL. */
  public static final String LEVEL = "level";

  /** The Constant LOGGER. */
  public static final String LOGGER = "logger";

  private final String loggerName;
  private final Level level;

  /**
   * Instantiates a new log4j level changer.
   * 
   * @param loggerName
   *          the logger name
   * @param level
   *          the level
   */
  public Log4jLevelChanger(String loggerName, Level level) {
    this.level = level;
    this.loggerName = loggerName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * asia.stampy.examples.remote.exe.common.Remoteable#setProperties(java.util
   * .Map)
   */
  @Override
  public void setProperties(Map<String, List<String>> properties) throws MissingPropertyException {
    // unimplemented
  }

  /*
   * (non-Javadoc)
   * 
   * @see asia.stampy.examples.remote.exe.common.Remoteable#execute()
   */
  @Override
  public boolean execute() throws Exception {
    Logger logger = getLogger();
    logger.setLevel(getLevel());

    return true;
  }

  private Logger getLogger() {
    return StringUtils.isEmpty(getLoggerName()) ? Logger.getRootLogger() : Logger.getLogger(getLoggerName());
  }

  /**
   * Gets the level.
   * 
   * @return the level
   */
  public Level getLevel() {
    return level;
  }

  /**
   * Gets the logger name.
   * 
   * @return the logger name
   */
  public String getLoggerName() {
    return loggerName;
  }

}
