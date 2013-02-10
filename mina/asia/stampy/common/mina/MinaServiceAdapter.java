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
package asia.stampy.common.mina;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaServiceAdapter implements IoServiceListener {

	public void serviceActivated(IoService service) throws Exception {
		// blank
	}

	public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
		// blank
	}

	public void serviceDeactivated(IoService service) throws Exception {
		// blank
	}

	public void sessionCreated(IoSession session) throws Exception {
		// blank
	}

	public void sessionDestroyed(IoSession session) throws Exception {
		// blank
	}

}
