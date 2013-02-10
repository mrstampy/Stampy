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

import asia.stampy.common.message.StampyMessage;


// TODO: Auto-generated Javadoc
/**
 * The Class ClientHandlerAdapter.
 */
public class ClientHandlerAdapter {
	
	/**
	 * Checks if is valid message.
	 *
	 * @param message the message
	 * @return true, if is valid message
	 */
	boolean isValidMessage(StampyMessage<?> message) {
		switch(message.getMessageType()) {
		
		case CONNECTED:
		case ERROR:
		case MESSAGE:
		case RECEIPT:
			message.validate();
			return true;
		default:
			return false;
		
		}
	}

}
