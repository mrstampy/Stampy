package asia.stampy.server.mina.test;


import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.server.mina.AbortMessageListener;
import asia.stampy.server.mina.AckMessageListener;
import asia.stampy.server.mina.BeginMessageListener;
import asia.stampy.server.mina.CommitMessageListener;
import asia.stampy.server.mina.ConnectMessageListener;
import asia.stampy.server.mina.DisconnectMessageListener;
import asia.stampy.server.mina.NackMessageListener;
import asia.stampy.server.mina.RawServerMinaHandler;
import asia.stampy.server.mina.SendMessageListener;
import asia.stampy.server.mina.ServerHeartbeatListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;
import asia.stampy.server.mina.SubscribeMessageListener;
import asia.stampy.server.mina.UnsubscribeMessageListener;

public class Initializer {

	public static ServerMinaMessageGateway initialize() {
		HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

		ServerMinaMessageGateway gateway = new ServerMinaMessageGateway();
		gateway.setPort(1234);

		// ServerMinaHandler handler = new ServerMinaHandler();
		RawServerMinaHandler handler = new RawServerMinaHandler();
		handler.setHeartbeatContainer(heartbeatContainer);
		handler.setMessageGateway(gateway);

		handler.addMessageListener(new AbortMessageListener());
		handler.addMessageListener(new AckMessageListener());
		handler.addMessageListener(new BeginMessageListener());
		handler.addMessageListener(new CommitMessageListener());
		handler.addMessageListener(new ConnectMessageListener());
		handler.addMessageListener(new DisconnectMessageListener());
		handler.addMessageListener(new NackMessageListener());
		handler.addMessageListener(new SendMessageListener());
		handler.addMessageListener(new SubscribeMessageListener());
		handler.addMessageListener(new UnsubscribeMessageListener());

		ServerHeartbeatListener hbListener = new ServerHeartbeatListener();
		hbListener.setHeartbeatContainer(heartbeatContainer);
		hbListener.setMessageGateway(gateway);

		handler.addMessageListener(hbListener);

		gateway.setHandler(handler);

		return gateway;
	}

}
