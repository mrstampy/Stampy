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

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;

import javax.annotation.Resource;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.StampyMinaHandler;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.common.mina.StampyServiceAdapter;

/**
 * The Class ClientMinaMessageGateway.
 */
@Resource
public class ClientMinaMessageGateway extends AbstractStampyMinaMessageGateway {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private StampyServiceAdapter serviceAdapter = new StampyServiceAdapter();
	private StampyMinaHandler<ClientMinaMessageGateway> handler;
	private NioSocketConnector connector;
	private int maxMessageSize = Integer.MAX_VALUE;
	private String host;
	private int port;

	private void init() {
		log.trace("Initializing Stampy MINA connector");

		connector = new NioSocketConnector();

		connector.setHandler(handler);

		connector.addListener(serviceAdapter);

		DefaultIoFilterChainBuilder chain = connector.getFilterChain();

		MdcInjectionFilter mdcInjectionFilter = new MdcInjectionFilter();
		chain.addLast("mdc", mdcInjectionFilter);
		chain.addLast("codec", new ProtocolCodecFilter(getHandler().getFactory(getMaxMessageSize())));

		log.trace("Connector initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see asia.stampy.common.AbstractStampyMessageGateway#connect()
	 */
	public void connect() throws Exception {
		log.trace("connect() invoked");
		if (connector == null || connector.isDisposed()) init();

		ConnectFuture cf = connector.connect(new InetSocketAddress(getHost(), getPort()));

		cf.await(2000);
		if (connector.isActive()) {
			log.info("Stampy MINA ClientMinaMessageGateway connected to {}:{}", host, port);
		} else {
			log.error("Could not connect to {}:{}", host, port);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.AbstractStampyMessageGateway#isConnected(asia.stampy
	 * .common.HostPort)
	 */
	@Override
	public boolean isConnected(HostPort hostPort) {
		return serviceAdapter.hasSession(hostPort) && connector.isActive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.AbstractStampyMessageGateway#closeConnection(asia.stampy
	 * .common.HostPort)
	 */
	@Override
	public void closeConnection(HostPort hostPort) {
		connector.dispose(true);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see asia.stampy.common.AbstractStampyMessageGateway#shutdown()
	 */
	@Override
	public void shutdown() throws Exception {
		closeConnection(null);
	}

	/**
	 * Send message. Use
	 * {@link ClientMinaMessageGateway#broadcastMessage(StampyMessage)} in
	 * preference.
	 * 
	 * @param stompMessage
	 *          the stomp message
	 * @param hostPort
	 *          the host port
	 */
	@Override
	public void sendMessage(String message, HostPort hostPort) throws InterceptException {
		broadcastMessage(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.AbstractStampyMessageGateway#broadcastMessage(java.lang
	 * .String)
	 */
	@Override
	public void broadcastMessage(String message) throws InterceptException {
		if (!connector.isActive()) {
			log.warn("Attempting to send message {} when the connector is not active", message);
			throw new IllegalStateException("The connector is not active, cannot send message");
		}

		interceptOutgoingMessage(message);

		for (HostPort hostPort : serviceAdapter.getHostPorts()) {
			getHandler().getHeartbeatContainer().reset(hostPort);
		}
		connector.broadcast(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#addMessageListener
	 * (asia.stampy.common.mina.StampyMinaMessageListener)
	 */
	@Override
	public void addMessageListener(StampyMinaMessageListener listener) {
		getHandler().addMessageListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#removeMessageListener
	 * (asia.stampy.common.mina.StampyMinaMessageListener)
	 */
	@Override
	public void removeMessageListener(StampyMinaMessageListener listener) {
		getHandler().removeMessageListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#clearMessageListeners
	 * ()
	 */
	@Override
	public void clearMessageListeners() {
		getHandler().clearMessageListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * asia.stampy.common.mina.AbstractStampyMinaMessageGateway#setListeners(java
	 * .util.Queue)
	 */
	@Override
	public void setListeners(Queue<StampyMinaMessageListener> listeners) {
		getHandler().setListeners(listeners);
	}

	@Override
	public void addServiceListener(IoServiceListener listener) {
		connector.addListener(listener);
	}

	@Override
	public void removeServiceListener(IoServiceListener listener) {
		connector.removeListener(listener);
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
	 * @param maxMessageSize
	 *          the new max message size
	 */
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 * 
	 * @param host
	 *          the new host
	 */
	public void setHost(String host) {
		this.host = host;
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
	 * @param port
	 *          the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the handler.
	 * 
	 * @return the handler
	 */
	public StampyMinaHandler<ClientMinaMessageGateway> getHandler() {
		return handler;
	}

	/**
	 * Sets the handler.
	 * 
	 * @param handler
	 *          the new handler
	 */
	public void setHandler(StampyMinaHandler<ClientMinaMessageGateway> handler) {
		this.handler = handler;
	}

}
