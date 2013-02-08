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
package asia.stampy.examples.loadtest.server;


import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.server.mina.AbortMessageListener;
import asia.stampy.server.mina.AckMessageListener;
import asia.stampy.server.mina.BeginMessageListener;
import asia.stampy.server.mina.CommitMessageListener;
import asia.stampy.server.mina.ConnectMessageListener;
import asia.stampy.server.mina.DisconnectMessageListener;
import asia.stampy.server.mina.NackMessageListener;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.SendMessageListener;
import asia.stampy.server.mina.ServerHeartbeatListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.SubscribeMessageListener;
import asia.stampy.server.mina.UnsubscribeMessageListener;


/**
 * This class programmatically initializes the Stampy classes required for this
 * example. It is expected that a DI framework such as <a
 * href="http://www.springsource.org/">Spring</a> or <a
 * href="http://code.google.com/p/google-guice/">Guice</a> will be used to
 * perform this task.
 */
public class Initializer {

	/**
	 * Initialize.
	 *
	 * @return the server mina message gateway
	 */
	public static ServerMinaMessageGateway initialize() {
		HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

		ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
		gateway.setPort(1234);

		// ServerMinaHandler handler = new ServerMinaHandler();
		RawServerMinaHandler handler = new RawServerMinaHandler();
		handler.setHeartbeatContainer(heartbeatContainer);
		handler.setMessageGateway(gateway);

		handler.addMessageListener(new AbortMessageListener());
		handler.addMessageListener(new AckMessageListener());
		handler.addMessageListener(new BeginMessageListener());
		handler.addMessageListener(new CommitMessageListener());
		handler.addMessageListener(new ConnectMessageListener());
		handler.addMessageListener(new DisconnectMessageListener());
		handler.addMessageListener(new NackMessageListener());
		handler.addMessageListener(new SendMessageListener());
		handler.addMessageListener(new SubscribeMessageListener());
		handler.addMessageListener(new UnsubscribeMessageListener());

		ServerHeartbeatListener hbListener = new ServerHeartbeatListener();
		hbListener.setHeartbeatContainer(heartbeatContainer);
		hbListener.setMessageGateway(gateway);

		handler.addMessageListener(hbListener);

		gateway.setHandler(handler);

		return gateway;
	}

}
