package asia.stampy.server.mina;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.ClientMessageHeader;
import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

class ServerHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private AbstractStampyMessageGateway messageGateway;

	boolean isValidMessage(StampyMessage<?> message) {
		switch (message.getMessageType()) {

		case ABORT:
		case ACK:
		case BEGIN:
		case COMMIT:
		case CONNECT:
		case STOMP:
		case DISCONNECT:
		case NACK:
		case SEND:
		case SUBSCRIBE:
		case UNSUBSCRIBE:
			return true;
		default:
			return false;

		}
	}

	void errorHandle(StampyMessage<?> message, Exception e, HostPort hostPort) {
		log.error("Handling error, sending error message to " + hostPort, e);
		String receipt = null;
		if (!message.getMessageType().equals(StampyMessageType.CONNECT)) {
			receipt = ((ClientMessageHeader) message.getHeader()).getReceipt();
		}

		ErrorMessage error = new ErrorMessage(receipt);
		error.getHeader().setMessageHeader(
				"Could not execute " + message.getClass().getCanonicalName() + ": " + e.getMessage());
		error.setBody(e);

		getMessageGateway().sendMessage(error, hostPort);
	}

	void sendResponseIfRequired(StampyMessage<?> message, IoSession session, HostPort hostPort) {
		if (isConnectMessage(message)) {
			sendConnected(((ConnectMessage) message).getHeader(), session, hostPort);
			log.debug("Sent CONNECTED message to {}", hostPort);
			return;
		}

		if (isStompMessage(message)) {
			sendConnected(((StompMessage) message).getHeader(), session, hostPort);
			log.debug("Sent CONNECTED message to {}", hostPort);
			return;
		}
		
		if(isDisconnectMessage(message)) {
			log.info("Disconnect message received, closing session", hostPort);
			session.close(false);
			return;
		}

		String receipt = ((ClientMessageHeader) message.getHeader()).getReceipt();
		if (StringUtils.isEmpty(receipt)) return;

		ReceiptMessage msg = new ReceiptMessage(receipt);

		getMessageGateway().sendMessage(msg, hostPort);
		log.debug("Sent RECEIPT message to {}", hostPort);
	}

	private void sendConnected(ConnectHeader header, IoSession session, HostPort hostPort) {
		ConnectedMessage message = new ConnectedMessage("1.2");
		message.getHeader().setHeartbeat(header.getClientHeartbeat(), header.getServerHeartbeat());
		message.getHeader().setSession(Long.toString(session.getId()));
		getMessageGateway().sendMessage(message, hostPort);
	}

	private boolean isConnectMessage(StampyMessage<?> message) {
		return message.getMessageType().equals(StampyMessageType.CONNECT);
	}

	private boolean isStompMessage(StampyMessage<?> message) {
		return message.getMessageType().equals(StampyMessageType.STOMP);
	}

	private boolean isDisconnectMessage(StampyMessage<?> message) {
		return message.getMessageType().equals(StampyMessageType.DISCONNECT);
	}

	public AbstractStampyMessageGateway getMessageGateway() {
		return messageGateway;
	}

	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		this.messageGateway = messageGateway;
	}

}
