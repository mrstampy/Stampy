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
package asia.stampy.examples.remote.exe.log4j.server.mina;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.examples.common.IDontNeedSecurity;
import asia.stampy.examples.remote.exe.common.RemoteExeMessageListener;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.receipt.MinaReceiptListener;

/**
 * This class programmatically initializes the Stampy classes required for this
 * example. It is expected that a DI framework such as <a
 * href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
@StampyLibrary(libraryName = "stampy-examples")
public class MinaInitializer {

  /**
   * Initialize.
   * 
   * @return the server mina message gateway
   */
  public static AbstractStampyMessageGateway initialize() {
    StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

    ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
    gateway.setPort(1234);

    RawServerMinaHandler handler = new RawServerMinaHandler();
    handler.setHeartbeatContainer(heartbeatContainer);
    handler.setGateway(gateway);
    
    gateway.addMessageListener(new IDontNeedSecurity());
    
    MinaReceiptListener receipt = new MinaReceiptListener();
    receipt.setGateway(gateway);
    gateway.addMessageListener(receipt);

    RemoteExeMessageListener remoteExe = new RemoteExeMessageListener();
    remoteExe.setGateway(gateway);
    gateway.addMessageListener(remoteExe);

    gateway.setHandler(handler);

    return gateway;
  }

}
