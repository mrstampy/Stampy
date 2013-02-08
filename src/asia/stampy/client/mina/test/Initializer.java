package asia.stampy.client.mina.test;


import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.client.mina.ConnectedMessageListener;
import asia.stampy.client.mina.ErrorMessageListener;
import asia.stampy.client.mina.MessageMessageListener;
import asia.stampy.client.mina.RawClientMinaHandler;
import asia.stampy.client.mina.ReceiptMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;

public class Initializer {

	public static ClientMinaMessageGateway initialize() {
		HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

		ClientMinaMessageGateway gateway = new ClientMinaMessageGateway();
		gateway.setPort(1234);
		gateway.setHost("localhost");

		// ClientMinaHandler handler = new ClientMinaHandler();
		RawClientMinaHandler handler = new RawClientMinaHandler();
		handler.setHeartbeatContainer(heartbeatContainer);
		handler.setMessageGateway(gateway);

		handler.addMessageListener(new ErrorMessageListener());
		handler.addMessageListener(new MessageMessageListener());
		handler.addMessageListener(new ReceiptMessageListener());

		ConnectedMessageListener cml = new ConnectedMessageListener();
		cml.setHeartbeatContainer(heartbeatContainer);
		cml.setMessageGateway(gateway);
		handler.addMessageListener(cml);

		gateway.setHandler(handler);

		return gateway;

	}
}
