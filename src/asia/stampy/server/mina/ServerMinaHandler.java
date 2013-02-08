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
package asia.stampy.server.mina;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.StampyMinaHandler;


/**
 * The Class ServerMinaHandler.
 */
public class ServerMinaHandler extends StampyMinaHandler {

	private ServerHandlerAdapter adapter = new ServerHandlerAdapter();

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaHandler#isValidMessage(asia.stampy.common.message.StampyMessage)
	 */
	@Override
	protected boolean isValidMessage(StampyMessage<?> message) {
		return adapter.isValidMessage(message);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaHandler#errorHandle(asia.stampy.common.message.StampyMessage, java.lang.Exception, org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
	 */
	protected void errorHandle(StampyMessage<?> message, Exception e, IoSession session, HostPort hostPort) {
		adapter.errorHandle(message, e, hostPort);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaHandler#sendResponseIfRequired(asia.stampy.common.message.StampyMessage, org.apache.mina.core.session.IoSession, asia.stampy.common.HostPort)
	 */
	protected void sendResponseIfRequired(StampyMessage<?> message, IoSession session, HostPort hostPort) {
		adapter.sendResponseIfRequired(message, session, hostPort);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaHandler#setMessageGateway(asia.stampy.common.AbstractStampyMessageGateway)
	 */
	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		super.setMessageGateway(messageGateway);
		adapter.setMessageGateway(messageGateway);
	}

}
