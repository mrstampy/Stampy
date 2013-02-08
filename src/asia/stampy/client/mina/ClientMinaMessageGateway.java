package asia.stampy.client.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.StampyMinaHandler;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.common.mina.StampyServiceAdapter;

public class ClientMinaMessageGateway extends AbstractStampyMinaMessageGateway {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private StampyServiceAdapter serviceAdapter = new StampyServiceAdapter();
	private StampyMinaHandler handler;
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

	public void connect() throws Exception {
		log.trace("connect() invoked");
		if (connector == null || connector.isDisposed()) init();

		ConnectFuture cf = connector.connect(new InetSocketAddress(getHost(), getPort()));

		cf.await(2000);
		if(connector.isActive()) {
			log.info("Stampy MINA ClientMinaMessageGateway connected to {}:{}", host, port);
		} else {
			log.error("Could not connect to {}:{}", host, port);
		}
	}

	@Override
	public boolean isConnected(HostPort hostPort) {
		return serviceAdapter.hasSession(hostPort) && connector.isActive();
	}

	@Override
	public void closeConnection(HostPort hostPort) {
		connector.dispose(true);
		init();
	}

	@Override
	public void shutdown() throws Exception {
		closeConnection(null);
	}

	/**
	 * @deprecated use {@link ClientMinaMessageGateway#broadcastMessage(StampyMessage)}
	 */
	@Override
	public void sendMessage(String stompMessage, HostPort hostPort) {
		broadcastMessage(stompMessage);
	}

	@Override
	public void broadcastMessage(String stompMessage) {
		if(! connector.isActive()) {
			log.warn("Attempting to send message {} when the connector is not active", stompMessage);
			throw new IllegalStateException("The connector is not active, cannot send message");
		}
		
		for(HostPort hostPort : serviceAdapter.getHostPorts()) {
			getHandler().getHeartbeatContainer().reset(hostPort);
		}
		connector.broadcast(stompMessage);
	}

	@Override
	public void addMessageListener(StampyMinaMessageListener listener) {
		getHandler().addMessageListener(listener);
	}

	@Override
	public void removeMessageListener(StampyMinaMessageListener listener) {
		getHandler().removeMessageListener(listener);
	}

	@Override
	public void clearMessageListeners() {
		getHandler().clearMessageListeners();
	}

	@Override
	public void setListeners(Queue<StampyMinaMessageListener> listeners) {
		getHandler().setListeners(listeners);
	}

	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public StampyMinaHandler getHandler() {
		return handler;
	}

	public void setHandler(StampyMinaHandler handler) {
		this.handler = handler;
	}

}
