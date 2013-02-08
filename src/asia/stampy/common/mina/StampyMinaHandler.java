package asia.stampy.common.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.StompMessageParser;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;

public abstract class StampyMinaHandler extends IoHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Queue<StampyMinaMessageListener> listeners = new ConcurrentLinkedQueue<>();

	private StompMessageParser parser = new StompMessageParser();

	private HeartbeatContainer heartbeatContainer;

	private AbstractStampyMessageGateway messageGateway;

	private static final String ILLEGAL_ACCESS_ATTEMPT = "Illegal access attempt";

	private Executor executor = Executors.newSingleThreadExecutor();
	
	public static Charset CHARSET = Charset.forName("UTF-8");

	public void messageReceived(final IoSession session, Object message) throws Exception {
		final HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
		log.debug("Received raw message {} from {}", message, hostPort);

		resetHeartbeat(hostPort);

		if (!isValidObject(message)) {
			log.error("Object {} is not a valid STOMP message, closing connection {}", message, hostPort);
			illegalAccess(session);
			return;
		}

		final String msg = (String) message;

		if (isHeartbeat(msg)) {
			log.debug("Simple heartbeat received");
			return;
		}

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				asyncProcessing(session, hostPort, msg);
			}
		};

		getExecutor().execute(runnable);
	}

	public ProtocolCodecFactory getFactory(int maxMessageSize) {
		PrefixedStringCodecFactory factory = new PrefixedStringCodecFactory(CHARSET);
		factory.setDecoderMaxDataLength(maxMessageSize);
		factory.setEncoderMaxDataLength(maxMessageSize);

		return factory;
	}

	protected void asyncProcessing(IoSession session, HostPort hostPort, String msg) {
		try {
			securityCheck(msg, session);

			StampyMessage<?> sm = getParser().parseMessage(msg);

			try {
				if (isValidMessage(sm)) {
					notifyListeners(sm, session, hostPort);
					sendResponseIfRequired(sm, session, hostPort);
				}
			} catch (Exception e) {
				errorHandle(sm, e, session, hostPort);
			}
		} catch (Exception e) {
			log.error("Unexpected exception processing message " + msg, e);
		}
	}

	protected void securityCheck(String msg, IoSession session) {

	}

	protected void securityCheck(StampyMessage<?> message, IoSession session) {

	}

	protected void illegalAccess(IoSession session) {
		session.write(ILLEGAL_ACCESS_ATTEMPT);
		session.close(false);
	}

	protected boolean isValidObject(Object message) {
		return message instanceof String;
	}

	protected boolean isHeartbeat(String msg) {
		return PaceMaker.HB1.equals(msg) || PaceMaker.HB2.equals(msg);
	}

	protected void sendResponseIfRequired(StampyMessage<?> sm, IoSession session, HostPort hostPort) {
		// Blank by dflt
	}

	protected void errorHandle(StampyMessage<?> message, Exception e, IoSession session, HostPort hostPort)
			throws Exception {
		log.error("Unexpected exception", e);
	}

	protected void resetHeartbeat(HostPort hostPort) {
		getHeartbeatContainer().reset(hostPort);
	}

	protected abstract boolean isValidMessage(StampyMessage<?> message);

	protected void notifyListeners(StampyMessage<?> sm, IoSession session, HostPort hostPort) throws Exception {
		securityCheck(sm, session);
		for (final StampyMinaMessageListener listener : listeners) {
			if (isForType(listener.getMessageTypes(), sm.getMessageType()) && listener.isForMessage(sm)) {
				log.debug("Executing message {} for listener {}", sm, listener);
				listener.messageReceived(sm, session, hostPort);
			}
		}
	}

	private boolean isForType(StampyMessageType[] messageTypes, StampyMessageType messageType) {
		for(StampyMessageType type : messageTypes) {
			if(type.equals(messageType)) return true;
		}

		return false;
	}

	public void addMessageListener(StampyMinaMessageListener listener) {
		listeners.add(listener);
	}

	public void removeMessageListener(StampyMinaMessageListener listener) {
		listeners.remove(listener);
	}

	public void clearMessageListeners() {
		listeners.clear();
	}

	public void setListeners(Queue<StampyMinaMessageListener> listeners) {
		this.listeners = listeners;
	}

	public StompMessageParser getParser() {
		return parser;
	}

	public void setParser(StompMessageParser parser) {
		this.parser = parser;
	}

	public HeartbeatContainer getHeartbeatContainer() {
		return heartbeatContainer;
	}

	public void setHeartbeatContainer(HeartbeatContainer heartbeatContainer) {
		this.heartbeatContainer = heartbeatContainer;
	}

	public AbstractStampyMessageGateway getMessageGateway() {
		return messageGateway;
	}

	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		this.messageGateway = messageGateway;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

}
