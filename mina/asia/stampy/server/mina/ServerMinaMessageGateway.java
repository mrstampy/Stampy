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

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.StampyMinaHandler;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.common.mina.StampyServiceAdapter;


/**
 * The Class ServerMinaMessageGateway.
 */
@Resource
public class ServerMinaMessageGateway extends AbstractStampyMinaMessageGateway {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private StampyServiceAdapter serviceAdapter = new StampyServiceAdapter();
	private StampyMinaHandler<ServerMinaMessageGateway> handler;
	private NioSocketAcceptor acceptor;
	private int maxMessageSize = Integer.MAX_VALUE;
	private int port;

	private void init() {
		log.trace("Initializing Stampy MINA acceptor");
		
		serviceAdapter.setGateway(this);
		serviceAdapter.setAutoShutdown(isAutoShutdown());
		
		acceptor = new NioSocketAcceptor();

		acceptor.setReuseAddress(true);
		acceptor.setCloseOnDeactivation(true);

		acceptor.setHandler(handler);

		acceptor.addListener(serviceAdapter);

		DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

		MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
		chain.addLast("mdc", mdcInjectionFilter);
		chain.addLast("codec", new ProtocolCodecFilter(getHandler().getFactory(getMaxMessageSize())));
		log.trace("Acceptor initialized");
	}

	/**
	 * Sends a {@link StampyMessage} to the specified {@link HostPort}. Use this
	 * method for all STOMP messages.
	 * 
	 * @param message
	 *          the message
	 * @param hostPort
	 *          the host port
	 * @throws InterceptException 
	 */
	public void sendMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
		interceptOutgoingMessage(message, hostPort);
		sendMessage(message.toStompMessage(true), hostPort);
	}

	protected void interceptOutgoingMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
		for (StampyOutgoingMessageInterceptor interceptor : interceptors) {
			if (isForType(interceptor.getMessageTypes(), message.getMessageType()) && interceptor.isForMessage(message)) {
				interceptor.interceptMessage(message, hostPort);
			}
		}
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.AbstractStampyMessageGateway#connect()
	 */
	@Override
	public void connect() throws Exception {
		if (acceptor == null || acceptor.isDisposed()) init();
		acceptor.bind(new InetSocketAddress(getPort()));
		log.info("connect() invoked, bound to port {}", getPort());
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.AbstractStampyMessageGateway#isConnected(asia.stampy.common.HostPort)
	 */
	@Override
	public boolean isConnected(HostPort hostPort) {
		return serviceAdapter.hasSession(hostPort) && acceptor.isActive();
	}

	public void sendMessage(String message, HostPort hostPort) throws InterceptException {
		if (!isConnected(hostPort)) {
			log.warn("Attempting to send message {} to {} when the acceptor is not active", message, hostPort);
			throw new IllegalStateException("The acceptor is not active, cannot send message");
		}
		
		interceptOutgoingMessage(message);
		
		getHandler().getHeartbeatContainer().reset(hostPort);
		serviceAdapter.sendMessage(message, hostPort);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.AbstractStampyMessageGateway#broadcastMessage(java.lang.String)
	 */
	@Override
	public void broadcastMessage(String message) throws InterceptException {
		if(! acceptor.isActive()) {
			log.warn("Attempting to broadcast {} when the acceptor is not active", message);
			throw new IllegalStateException("The acceptor is not active, cannot send message");
		}
		
		interceptOutgoingMessage(message);
		
		for(HostPort hostPort : serviceAdapter.getHostPorts()) {
			getHandler().getHeartbeatContainer().reset(hostPort);
		}
		
		acceptor.broadcast(message);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.AbstractStampyMessageGateway#closeConnection(asia.stampy.common.HostPort)
	 */
	@Override
	public void closeConnection(HostPort hostPort) {
		if (!serviceAdapter.hasSession(hostPort)) return;
		log.info("closeConnection() invoked, closing session for {}", hostPort);

		IoSession session = serviceAdapter.getSession(hostPort);
		session.close(false);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.AbstractStampyMessageGateway#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		log.info("shutdown() invoked, disposing the acceptor");
		acceptor.dispose(false);
		init();
	}

	@Override
	public Set<HostPort> getConnectedHostPorts() {
		return serviceAdapter.getHostPorts();
	}

	@Override
	public void addServiceListener(IoServiceListener listener) {
		acceptor.addListener(listener);
	}

	@Override
	public void removeServiceListener(IoServiceListener listener) {
		acceptor.removeListener(listener);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.AbstractStampyMinaMessageGateway#addMessageListener(asia.stampy.common.mina.StampyMinaMessageListener)
	 */
	@Override
	public void addMessageListener(StampyMinaMessageListener listener) {
		getHandler().addMessageListener(listener);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.AbstractStampyMinaMessageGateway#removeMessageListener(asia.stampy.common.mina.StampyMinaMessageListener)
	 */
	@Override
	public void removeMessageListener(StampyMinaMessageListener listener) {
		getHandler().removeMessageListener(listener);
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.AbstractStampyMinaMessageGateway#clearMessageListeners()
	 */
	@Override
	public void clearMessageListeners() {
		getHandler().clearMessageListeners();
	}

	/* (non-Javadoc)
	 * @see asia.stampy.common.mina.AbstractStampyMinaMessageGateway#setListeners(java.util.Queue)
	 */
	@Override
	public void setListeners(Queue<StampyMinaMessageListener> listeners) {
		getHandler().setListeners(listeners);
	}

	/**
	 * Gets the max message size.
	 *
	 * @return the max message size
	 */
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	/**
	 * Sets the max message size.
	 *
	 * @param maxMessageSize the new max message size
	 */
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the handler.
	 *
	 * @return the handler
	 */
	public StampyMinaHandler<ServerMinaMessageGateway> getHandler() {
		return handler;
	}

	/**
	 * Sets the handler.
	 *
	 * @param handler the new handler
	 */
	public void setHandler(StampyMinaHandler<ServerMinaMessageGateway> handler) {
		this.handler = handler;
	}

}
