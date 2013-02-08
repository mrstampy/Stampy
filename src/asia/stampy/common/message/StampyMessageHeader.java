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
package asia.stampy.common.message;

import java.io.Serializable;
import java.util.Map;


/**
 * The Interface StampyMessageHeader.
 */
public interface StampyMessageHeader extends Serializable {
	
	/**
	 * To message header.
	 *
	 * @return the string
	 */
	String toMessageHeader();

	/**
	 * Adds the header.
	 *
	 * @param key the key
	 * @param value the value
	 */
	void addHeader(String key, String value);
	
	/**
	 * Checks for header.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	boolean hasHeader(String key);
	
	/**
	 * Removes the header.
	 *
	 * @param key the key
	 */
	void removeHeader(String key);
	
	/**
	 * Gets the header value.
	 *
	 * @param key the key
	 * @return the header value
	 */
	String getHeaderValue(String key);
	
	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	Map<String, String> getHeaders();
}
