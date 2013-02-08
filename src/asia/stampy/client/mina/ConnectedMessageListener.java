package asia.stampy.client.mina;

import java.lang.invoke.MethodHandles;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.connected.ConnectedMessage;

public class ConnectedMessageListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private HeartbeatContainer heartbeatContainer;
	
	private AbstractStampyMessageGateway messageGateway;

	@Override
	public StampyMessageType[] getMessageTypes() {
		return new StampyMessageType[] { StampyMessageType.CONNECTED };
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		log.debug("Received connect message {} from {}", message, hostPort);
		ConnectedMessage cm = (ConnectedMessage) message;

		int clientHeartbeat = cm.getHeader().getClientHeartbeat();
		int serverHeartbeat = cm.getHeader().getServerHeartbeat();
		if (serverHeartbeat <= 0 || clientHeartbeat <= 0) return;

		log.info("Starting heartbeats for {} at {} ms intervals", hostPort, clientHeartbeat);
		PaceMaker paceMaker = new PaceMaker(clientHeartbeat);
		paceMaker.setHostPort(hostPort);
		paceMaker.setMessageGateway(getMessageGateway());
		getHeartbeatContainer().add(hostPort, paceMaker);
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

}
