package asia.stampy.server.mina;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.PaceMaker;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

public class ServerHeartbeatListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private HeartbeatContainer heartbeatContainer;

	private AbstractStampyMessageGateway messageGateway;

	@Override
	public StampyMessageType[] getMessageTypes() {
		return new StampyMessageType[] { StampyMessageType.CONNECT, StampyMessageType.STOMP, StampyMessageType.DISCONNECT };
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return StringUtils.isNotEmpty(message.getHeader().getHeaderValue(ConnectHeader.HEART_BEAT))
				|| isDisconnectMessage(message);
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		if (isDisconnectMessage(message)) {
			getHeartbeatContainer().remove(hostPort);
			return;
		}

		ConnectHeader header = getConnectHeader(message);

		int serverSleep = header.getServerHeartbeat();
		int clientSleep = header.getClientHeartbeat();
		if (clientSleep <= 0 || serverSleep <= 0) return;

		log.info("Starting heartbeats for {} at {} ms intervals", hostPort, serverSleep);
		PaceMaker paceMaker = new PaceMaker(serverSleep);
		paceMaker.setHostPort(hostPort);
		paceMaker.setMessageGateway(getMessageGateway());

		getHeartbeatContainer().add(hostPort, paceMaker);
	}

	public void resetHeartbeat(HostPort hostPort) {
		getHeartbeatContainer().reset(hostPort);
	}

	private ConnectHeader getConnectHeader(StampyMessage<?> message) {
		return isConnectMessage(message) ? ((ConnectMessage) message).getHeader() : ((StompMessage) message).getHeader();
	}

	private boolean isConnectMessage(StampyMessage<?> message) {
		return StampyMessageType.CONNECT.equals(message.getMessageType());
	}

	private boolean isDisconnectMessage(StampyMessage<?> message) {
		return StampyMessageType.DISCONNECT.equals(message.getMessageType());
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
