package asia.stampy.examples.system.client;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.STOMP;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

public class SystemClient {

	private static final StompMessageType[] CLIENT_TYPES = { ABORT, ACK, NACK, SEND, STOMP, SUBSCRIBE, UNSUBSCRIBE };

	private ClientMinaMessageGateway gateway;

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
				// TODO Auto-generated method stub

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
		gateway.broadcastMessage(new ConnectMessage("1.2", "localhost"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ClientMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ClientMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

}
