package asia.stampy.client.mina.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

public class TestClient {
	private ClientMinaMessageGateway gateway;

	private List<String> receipts = new ArrayList<>();

	private boolean connected = false;

	private int receiptId = 1;

	private Object waiter = new Object();

	private long start;
	private long end;
	
	private int times = 100000;

	public void init() throws Exception {
		setGateway(Initializer.initialize());
		gateway.addMessageListener(new StampyMinaMessageListener() {

			@Override
			public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
				switch (message.getMessageType()) {
				case CONNECTED:
					connected = true;
					start = System.nanoTime();
					sendAcks();
					break;
				case ERROR:
					System.out.println("Unexpected error " + ((ErrorMessage) message).getHeader().getMessageHeader());
					break;
				case RECEIPT:
					receipts.add(((ReceiptMessage) message).getHeader().getReceiptId());
					receiptId++;

					if (receiptId >= times) {
						synchronized (waiter) {
							waiter.notifyAll();
						}
					}

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
		gateway.broadcastMessage(new ConnectMessage("1.2", "localhost"));
	}

	public void disconnect() throws Exception {
		synchronized (waiter) {
			waiter.wait();
		}
		gateway.broadcastMessage(new DisconnectMessage());
		end = System.nanoTime();
	}

	public void stats() {
		System.out.println("# of receipts: " + receipts.size());
		System.out.println("Connected message? " + connected);
		long diff = end - start;
		System.out.println("Nano time elapsed: " + diff);
	}

	private void sendAcks() throws Exception {
		for (int i = 1; i <= times; i++) {
			sendAck(i);
		}
	}

	private void sendAck(int i) {
		String id = Integer.toString(i);
		AckMessage ack = new AckMessage(id);
		ack.getHeader().setReceipt(id);
		ack.getHeader().setTransaction(id);

		gateway.broadcastMessage(ack);
	}

	public ClientMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ClientMinaMessageGateway gateway) {
		this.gateway = gateway;
	}

	public static void main(String[] args) {
		TestClient client = new TestClient();
		try {
			client.init();
			client.disconnect();
			client.stats();
			client.getGateway().shutdown();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
