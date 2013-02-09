package asia.stampy.server.mina.login;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.mina.ServerMinaMessageGateway;

@Resource
public class LoginMessageListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static StompMessageType[] TYPES = StompMessageType.values();

	private Queue<HostPort> loggedInConnections = new ConcurrentLinkedQueue<>();

	private StampyLoginHandler loginHandler;
	private ServerMinaMessageGateway gateway;

	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		switch (message.getMessageType()) {
		case ABORT:
		case ACK:
		case BEGIN:
		case COMMIT:
		case NACK:
		case SEND:
		case SUBSCRIBE:
		case UNSUBSCRIBE:
			loggedInCheck(message, hostPort);
			break;
		case CONNECT:
			logIn(session, hostPort, ((ConnectMessage) message).getHeader());
			break;
		case STOMP:
			logIn(session, hostPort, ((StompMessage) message).getHeader());
			break;
		case DISCONNECT:
			loggedInConnections.remove(hostPort);
			break;
		default:
			String error = "Unexpected message type " + message.getMessageType();
			log.error(error);
			throw new IllegalArgumentException(error);

		}
	}

	private void loggedInCheck(StampyMessage<?> message, HostPort hostPort) throws NotLoggedInException {
		if (loggedInConnections.contains(hostPort)) return;

		log.error("{} attempted to send a {} message without logging in", hostPort, message.getMessageType());
		throw new NotLoggedInException("Not logged in");
	}

	private void logIn(IoSession session, HostPort hostPort, ConnectHeader header) throws AlreadyLoggedInException,
			NotLoggedInException {
		if (loggedInConnections.contains(hostPort)) throw new AlreadyLoggedInException(hostPort + " is already logged in");

		if (!isForHeader(header)) throw new NotLoggedInException("login and passcode not specified, cannot log in");

		try {
			getLoginHandler().login(header.getLogin(), header.getPasscode());
		} catch (TerminateSessionException e) {
			log.error(e.getMessage(), e);
			sendErrorMessage(e.getMessage(), hostPort);
			session.close(false);
		}
	}

	private void sendErrorMessage(String message, HostPort hostPort) {
		ErrorMessage error = new ErrorMessage("n/a");
		error.getHeader().setMessageHeader(message);

		try {
			getGateway().sendMessage(error, hostPort);
		} catch (InterceptException e) {
			log.error("Sending of login error message failed", e);
		}
	}

	private boolean isForHeader(ConnectHeader header) {
		return StringUtils.isNotEmpty(header.getLogin()) && StringUtils.isNotEmpty(header.getPasscode());
	}

	public StampyLoginHandler getLoginHandler() {
		return loginHandler;
	}

	public void setLoginHandler(StampyLoginHandler loginHandler) {
		this.loginHandler = loginHandler;
	}

	public ServerMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;
		gateway.addServiceListener(new LoginTerminatorAdapter());
	}
	
	private class LoginTerminatorAdapter extends MinaServiceAdapter {

		public void sessionDestroyed(IoSession session) throws Exception {
			HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
			if(loggedInConnections.contains(hostPort)) {
				log.info("{} session terminated before DISCONNECT message received, cleaning up", hostPort);
				loggedInConnections.remove(hostPort);
			}
		}

	}

}
