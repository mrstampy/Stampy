package asia.stampy.examples.system.server;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

public class SystemServer {

	private ServerMinaMessageGateway gateway;

	/**
	 * Inits.
	 * 
	 * @throws Exception
	 *           the exception
	 */
	public void init() throws Exception {
		setGateway(SystemServerInitializer.initialize());

		gateway.addMessageListener(new StampyMinaMessageListener() {

			@Override
			public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
				switch (message.getMessageType()) {
				case ABORT:
					break;
				case ACK:
					break;
				case BEGIN:
					break;
				case COMMIT:
					break;
				case CONNECT:
					break;
				case DISCONNECT:
					break;
				case NACK:
					break;
				case SEND:
					break;
				case STOMP:
					break;
				case SUBSCRIBE:
					break;
				case UNSUBSCRIBE:
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
		System.out.println("Stampy system server started");
	}

	public ServerMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
