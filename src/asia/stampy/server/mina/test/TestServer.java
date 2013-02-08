package asia.stampy.server.mina.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

public class TestServer {
	private ServerMinaMessageGateway gateway;

	private List<String> acks = new ArrayList<>();

	private boolean connect = false;
	private boolean disconnect = false;
	
	private long start;
	private long end;

	public void init() throws Exception {
		setGateway(Initializer.initialize());

		gateway.addMessageListener(new StampyMinaMessageListener() {

			@Override
			public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
				switch (message.getMessageType()) {
				case ACK:
					acks.add(((AckMessage) message).getHeader().getId());
					break;
				case CONNECT:
					connect = true;
					start = System.nanoTime();
					break;
				case DISCONNECT:
					disconnect = true;
					end = System.nanoTime();
					stats();
					break;
				default:
					System.out.println("Unexpected message " + message.getMessageType());
					break;

				}
			}

			@Override
			public boolean isForMessage(StampyMessage<?> message) {
				return true;
			}

			@Override
			public StampyMessageType[] getMessageTypes() {
				return StampyMessageType.values();
			}
		});

		gateway.connect();
		System.out.println("Stampy server started");
	}

	public void stats() {
		System.out.println("# of acks: " + acks.size());
		System.out.println("Connect message? " + connect);
		System.out.println("Disconnect message? " + disconnect);
		long diff = end - start;
		System.out.println("Nano time elapsed: " + diff);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestServer server = new TestServer();
		try {
			server.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ServerMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

}
