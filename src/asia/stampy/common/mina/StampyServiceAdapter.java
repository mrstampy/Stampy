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

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;


/**
 * This class keeps track of all connections and disconnections and is the interface for
 * sending messages to remote hosts.
 */
@Resource
public class StampyServiceAdapter implements IoServiceListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Map<HostPort, IoSession> sessions = new ConcurrentHashMap<>();
	
	private boolean autoShutdown;
	
	private AbstractStampyMinaMessageGateway gateway;

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoServiceListener#sessionCreated(org.apache.mina.core.session.IoSession)
	 */
	public void sessionCreated(IoSession session) throws Exception {
		HostPort hostPort = createHostPort(session);
		log.info("Stampy MINA session created for {}", hostPort);
		
		sessions.put(hostPort, session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoServiceListener#sessionDestroyed(org.apache.mina.core.session.IoSession)
	 */
	public void sessionDestroyed(IoSession session) throws Exception {
		HostPort hostPort = createHostPort(session);
		log.info("Stampy MINA session destroyed for {}", hostPort);

		sessions.remove(hostPort);
		
		if(sessions.isEmpty() && isAutoShutdown()) {
			log.info("No more sessions and auto shutdown is true, shutting down gateway");
			gateway.shutdown();
		}
	}

	private HostPort createHostPort(IoSession session) {
		return new HostPort((InetSocketAddress)session.getRemoteAddress());
	}
	
	/**
	 * Returns true if the specified {@link HostPort} has an active session.
	 *
	 * @param hostPort the host port
	 * @return true, if successful
	 */
	public boolean hasSession(HostPort hostPort) {
		return sessions.containsKey(hostPort);
	}
	
	/**
	 * Gets the session.
	 *
	 * @param hostPort the host port
	 * @return the session
	 * @throws IllegalArgumentException if no active session
	 */
	public IoSession getSession(HostPort hostPort) {
		IoSession session = sessions.get(hostPort);
		
		if(session == null) throw new IllegalArgumentException(hostPort.toString() + " has no current session");
		
		return session;
	}
	
	/**
	 * Gets the host ports.
	 *
	 * @return the host ports
	 */
	public Set<HostPort> getHostPorts() {
		return sessions.keySet();
	}
	
	/**
	 * Send message.
	 *
	 * @param stompMessage the stomp message
	 * @param hostPort the host port
	 */
	public void sendMessage(String stompMessage, HostPort hostPort) {
		if(! hasSession(hostPort)) {
			return;
		}
		
		IoSession session = getSession(hostPort);
		session.write(stompMessage);
		log.debug("Sent message {} to {}", stompMessage, hostPort);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoServiceListener#serviceActivated(org.apache.mina.core.service.IoService)
	 */
	public void serviceActivated(IoService service) throws Exception {
		// blank
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoServiceListener#serviceIdle(org.apache.mina.core.service.IoService, org.apache.mina.core.session.IdleStatus)
	 */
	public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
		// blank
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.core.service.IoServiceListener#serviceDeactivated(org.apache.mina.core.service.IoService)
	 */
	public void serviceDeactivated(IoService service) throws Exception {
		// blank
	}

	public boolean isAutoShutdown() {
		return autoShutdown;
	}

	public void setAutoShutdown(boolean autoClose) {
		this.autoShutdown = autoClose;
	}

	public AbstractStampyMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(AbstractStampyMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

}
