package asia.stampy.server.mina.subscription;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;

import asia.stampy.client.message.ack.AckHeader;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.nack.NackHeader;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

@Resource
public class AcknowledgementListener implements StampyMinaMessageListener {
	private static final StompMessageType[] TYPES = { StompMessageType.ACK, StompMessageType.NACK };

	private MessageInterceptor interceptor;
	private StampyAcknowledgementHandler handler;

	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		switch (message.getMessageType()) {
		case ACK:
			evaluateAck(((AckMessage) message).getHeader(), hostPort);
			break;
		case NACK:
			evaluateNack(((NackMessage) message).getHeader(), hostPort);
			break;
		default:
			break;

		}
	}

	private void evaluateNack(NackHeader header, HostPort hostPort) throws Exception {
		String id = header.getId();
		if (getInterceptor().hasMessageAck(id, hostPort)) {
			getInterceptor().clearMessageAck(id, hostPort);
			getHandler().nackReceived(id, header.getReceipt(), header.getTransaction());
		} else {
			throw new UnexpectedAcknowledgementException("No NACK message expected, yet received id " + id + " from "
					+ hostPort);
		}
	}

	private void evaluateAck(AckHeader header, HostPort hostPort) throws Exception {
		String id = header.getId();
		if (getInterceptor().hasMessageAck(id, hostPort)) {
			getInterceptor().clearMessageAck(id, hostPort);
			getHandler().ackReceived(id, header.getReceipt(), header.getTransaction());
		} else {
			throw new UnexpectedAcknowledgementException("No ACK message expected, yet received id " + id + " from "
					+ hostPort);
		}
	}

	public MessageInterceptor getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(MessageInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public StampyAcknowledgementHandler getHandler() {
		return handler;
	}

	public void setHandler(StampyAcknowledgementHandler adapter) {
		this.handler = adapter;
	}

}
