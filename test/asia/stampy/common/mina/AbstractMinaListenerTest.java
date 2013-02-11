package asia.stampy.common.mina;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.apache.mina.core.session.IoSession;
import org.mockito.Mock;

import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.begin.BeginMessage;
import asia.stampy.client.message.commit.CommitMessage;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;
import asia.stampy.server.mina.ServerMinaMessageGateway;

public abstract class AbstractMinaListenerTest {
	protected HostPort hostPort = new HostPort("burt.alexander", 9999);

	@Mock
	protected IoSession session;

	@Mock
	protected ClientMinaMessageGateway clientGateway;

	@Mock
	protected ServerMinaMessageGateway serverGateway;

	protected void testTypes(StampyMinaMessageListener listener, StompMessageType[] expecteds) {
		StompMessageType[] actuals = listener.getMessageTypes();

		testTypes(expecteds, actuals);
	}

	protected void testTypes(StampyOutgoingMessageInterceptor interceptor, StompMessageType[] expecteds) {
		StompMessageType[] actuals = interceptor.getMessageTypes();

		testTypes(expecteds, actuals);
	}

	protected StampyMessage<?> getMessage(StompMessageType type) {
		switch (type) {
		case ABORT:
			return new AbortMessage("transaction");
		case ACK:
			return new AckMessage("id");
		case BEGIN:
			return new BeginMessage("transaction");
		case COMMIT:
			return new CommitMessage("transaction");
		case NACK:
			return new NackMessage("id");
		case SEND:
			return new SendMessage("destination", "receiptId");
		case SUBSCRIBE:
			return new SubscribeMessage("destination", "id");
		case UNSUBSCRIBE:
			return new UnsubscribeMessage("id");
		case CONNECT:
			return new ConnectMessage("host");
		case CONNECTED:
			return new ConnectedMessage("1.2");
		case DISCONNECT:
			return new DisconnectMessage();
		case ERROR:
			return new ErrorMessage("receiptId");
		case MESSAGE:
			return new MessageMessage("destination", "messageId", "subscription");
		case RECEIPT:
			return new ReceiptMessage("receiptId");
		case STOMP:
			return new StompMessage("host");
		default:
			throw new IllegalArgumentException(type + " is not recognized");
		}
	}

	private void testTypes(StompMessageType[] expecteds, StompMessageType[] actuals) {
		assertEquals(expecteds.length, actuals.length);

		for (StompMessageType expected : expecteds) {
			testTypes(expected, actuals);
		}
	}

	private void testTypes(StompMessageType expected, StompMessageType[] actuals) {
		boolean exists = false;
		for (StompMessageType actual : actuals) {
			if (actual.equals(expected)) exists = true;
		}

		assertTrue(exists);
	}

}
