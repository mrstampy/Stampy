package asia.stampy.examples.system.client;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.message.error.ErrorMessage;

public class SystemClient {

	private static final String IS_ALREADY_LOGGED_IN = "is already logged in";

	private static final String ONLY_STOMP_VERSION_1_2_IS_SUPPORTED = "Only STOMP version 1.2 is supported";

	private static final String LOGIN_AND_PASSCODE_NOT_SPECIFIED = "login and passcode not specified";

	private static final String NOT_LOGGED_IN = "Not logged in";

	private static final StompMessageType[] CLIENT_TYPES = { ABORT, ACK, NACK, SEND, SUBSCRIBE, UNSUBSCRIBE };

	private ClientMinaMessageGateway gateway;

	private ErrorMessage error;

	private Object waiter = new Object();

	private boolean connected;

	/**
	 * Inits the.
	 * 
	 * @throws Exception
	 *           the exception
	 */
	public void init() throws Exception {
		setGateway(SystemClientInitializer.initialize());
		gateway.addMessageListener(new StampyMinaMessageListener() {

			@Override
			public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
				switch (message.getMessageType()) {
				case CONNECTED:
					connected = true;
					wakeup();
					break;
				case ERROR:
					setError((ErrorMessage) message);
					wakeup();
					break;
				case MESSAGE:
					break;
				case RECEIPT:
					break;
				default:
					break;

				}
			}

			@Override
			public boolean isForMessage(StampyMessage<?> message) {
				return true;
			}

			@Override
			public StompMessageType[] getMessageTypes() {
				return StompMessageType.values();
			}
		});

		gateway.connect();
	}

	public void testConnect() throws Exception {
		for (int i = 0; i < CLIENT_TYPES.length; i++) {
			sendMessage(CLIENT_TYPES[i], Integer.toString(i));
			sleep();
			evaluateError(NOT_LOGGED_IN);
		}

		sendConnect("host");
		sleep();
		evaluateError(LOGIN_AND_PASSCODE_NOT_SPECIFIED);

		sendStomp("host");
		sleep();
		evaluateError(LOGIN_AND_PASSCODE_NOT_SPECIFIED);

		ConnectMessage message = new ConnectMessage("1.1", "burt.alexander");
		message.getHeader().setLogin(SystemLoginHandler.GOOD_USER);
		message.getHeader().setPasscode("pass");
		getGateway().broadcastMessage(message);
		sleep();
		evaluateError(ONLY_STOMP_VERSION_1_2_IS_SUPPORTED);

		message.getHeader().removeHeader(ConnectHeader.ACCEPT_VERSION);
		message.getHeader().setAcceptVersion("1.2");
		message.getHeader().setHeartbeat(50, 50);
		getGateway().broadcastMessage(message);
		sleep();
		evaluateConnect();

		getGateway().broadcastMessage(message);
		sleep();
		evaluateError(IS_ALREADY_LOGGED_IN);
	}

	private void evaluateConnect() {
		System.out.println("Is connected? " + connected);
		System.out.println();
	}

	private void evaluateError(String messagePart) {
		String msg = error.getHeader().getMessageHeader();
		if(msg.contains(messagePart)) {
			System.out.println("Expected error message received");
		} else {
			System.out.println("Unexpected error message received");
		}
		System.out.println(error.toStompMessage(false));
		System.out.println();
		error = null;
	}

	private void sendMessage(StompMessageType type, String id) throws InterceptException {
		switch (type) {
		case ABORT:
			sendAbort(id);
			break;
		case ACK:
			sendAck(id);
			break;
		case BEGIN:
			sendNack(id);
			break;
		case COMMIT:
			sendCommit(id);
			break;
		case CONNECT:
			sendConnect(id);
			break;
		case DISCONNECT:
			sendDisconnect(id);
			break;
		case NACK:
			sendNack(id);
			break;
		case SEND:
			sendSend(id);
			break;
		case STOMP:
			sendStomp(id);
			break;
		case SUBSCRIBE:
			sendSubscribe(id);
			break;
		case UNSUBSCRIBE:
			sendUnsubscribe(id);
			break;
		default:
			break;

		}
	}

	private void sendUnsubscribe(String id) throws InterceptException {
		UnsubscribeMessage message = new UnsubscribeMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sendSubscribe(String id) throws InterceptException {
		SubscribeMessage message = new SubscribeMessage("over/there", id);
		getGateway().broadcastMessage(message);
	}

	private void sendStomp(String id) throws InterceptException {
		StompMessage message = new StompMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sendSend(String id) throws InterceptException {
		SendMessage message = new SendMessage("over/there", id);
		getGateway().broadcastMessage(message);
	}

	private void sendDisconnect(String id) {
		// TODO Auto-generated method stub

	}

	private void sendConnect(String id) throws InterceptException {
		ConnectMessage message = new ConnectMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sendCommit(String id) {
		// TODO Auto-generated method stub

	}

	private void sendNack(String id) throws InterceptException {
		NackMessage message = new NackMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sendAck(String id) throws InterceptException {
		AckMessage message = new AckMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sendAbort(String id) throws InterceptException {
		AbortMessage message = new AbortMessage(id);
		getGateway().broadcastMessage(message);
	}

	private void sleep() throws InterruptedException {
		synchronized (waiter) {
			waiter.wait();
		}
	}

	private void wakeup() {
		synchronized (waiter) {
			waiter.notifyAll();
		}
	}

	public ErrorMessage getError() {
		return error;
	}

	public void setError(ErrorMessage error) {
		this.error = error;
	}

	public ClientMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ClientMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SystemClient client = new SystemClient();

		try {
			client.init();
			client.testConnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
