package asia.stampy.server.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.StampyMinaHandler;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.common.mina.StampyServiceAdapter;

public class ServerMinaMessageGateway extends AbstractStampyMinaMessageGateway {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private StampyServiceAdapter serviceAdapter = new StampyServiceAdapter();
	private StampyMinaHandler handler;
	private NioSocketAcceptor acceptor;
	private int maxMessageSize = Integer.MAX_VALUE;
	private int port;

	private void init() {
		log.trace("Initializing Stampy MINA acceptor");
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

	@Override
	public void connect() throws Exception {
		if (acceptor == null || acceptor.isDisposed()) init();
		acceptor.bind(new InetSocketAddress(getPort()));
		log.info("connect() invoked, bound to port {}", getPort());
	}

	@Override
	public boolean isConnected(HostPort hostPort) {
		return serviceAdapter.hasSession(hostPort) && acceptor.isActive();
	}

	@Override
	public void sendMessage(String stompMessage, HostPort hostPort) {
		if (!isConnected(hostPort)) {
			log.warn("Attempting to send message {} to {} when the acceptor is not active", stompMessage, hostPort);
			throw new IllegalStateException("The acceptor is not active, cannot send message");
		}
		
		getHandler().getHeartbeatContainer().reset(hostPort);
		serviceAdapter.sendMessage(stompMessage, hostPort);
	}

	@Override
	public void broadcastMessage(String stompMessage) {
		if(! acceptor.isActive()) {
			log.warn("Attempting to broadcast {} when the acceptor is not active", stompMessage);
			throw new IllegalStateException("The acceptor is not active, cannot send message");
		}
		
		for(HostPort hostPort : serviceAdapter.getHostPorts()) {
			getHandler().getHeartbeatContainer().reset(hostPort);
		}
		
		acceptor.broadcast(stompMessage);
	}

	@Override
	public void closeConnection(HostPort hostPort) {
		if (!serviceAdapter.hasSession(hostPort)) return;
		log.info("closeConnection() invoked, closing session for {}", hostPort);

		IoSession session = serviceAdapter.getSession(hostPort);
		session.close(false);
	}

	@Override
	public void shutdown() throws Exception {
		log.info("shutdown() invoked, disposing the acceptor");
		acceptor.dispose(true);
		init();
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
