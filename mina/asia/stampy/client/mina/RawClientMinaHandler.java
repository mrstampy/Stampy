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
package asia.stampy.client.mina;


import javax.annotation.Resource;

import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.raw.StampyRawStringHandler;


// TODO: Auto-generated Javadoc
/**
 * The Class RawClientMinaHandler.
 */
@Resource
public class RawClientMinaHandler extends StampyRawStringHandler<ClientMinaMessageGateway> {

	private ClientHandlerAdapter adapter = new ClientHandlerAdapter();

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.StampyMinaHandler#isValidMessage(asia.stampy.common.message.StampyMessage)
	 */
	@Override
	protected boolean isValidMessage(StampyMessage<?> message) {
		return adapter.isValidMessage(message);
	}

}
