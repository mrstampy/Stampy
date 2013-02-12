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
package asia.stampy.common.heartbeat;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;

/**
 * Encapsulates all the currently active {@link PaceMaker}s. This class is a
 * singleton; wire into the system appropriately.
 */
@Resource
public class HeartbeatContainer {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<HostPort, PaceMaker> paceMakers = new ConcurrentHashMap<>();

  /**
   * Stops heartbeats to the specified {@link HostPort}.
   * 
   * @param hostPort
   *          the host port
   */
  public void stop(HostPort hostPort) {
    PaceMaker paceMaker = paceMakers.get(hostPort);
    if (paceMaker != null) {
      log.info("Stopping PaceMaker for {}", hostPort);
      paceMaker.stop();
    }
  }

  /**
   * Adds a new {@link PaceMaker} for the specified {@link HostPort}.
   * 
   * @param hostPort
   *          the host port
   * @param paceMaker
   *          the pace maker
   */
  public void add(HostPort hostPort, PaceMaker paceMaker) {
    stop(hostPort);
    log.info("Adding PaceMaker for {}", hostPort);
    paceMakers.put(hostPort, paceMaker);
  }

  /**
   * Removes the {@link PaceMaker} specified by {@link HostPort}.
   * 
   * @param hostPort
   *          the host port
   */
  public void remove(HostPort hostPort) {
    stop(hostPort);
    log.info("Removing PaceMaker for {}", hostPort);
    paceMakers.remove(hostPort);
  }

  /**
   * Resets the {@link PaceMaker} for the specified {@link HostPort}, preventing
   * a heartbeat from being sent.
   * 
   * @param hostPort
   *          the host port
   */
  public void reset(HostPort hostPort) {
    log.trace("Resetting PaceMaker for {}", hostPort);
    PaceMaker paceMaker = paceMakers.get(hostPort);
    if (paceMaker != null) paceMaker.reset();
  }
}
