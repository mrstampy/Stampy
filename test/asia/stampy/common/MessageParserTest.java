package asia.stampy.common;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

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
import asia.stampy.common.HostPort;
import asia.stampy.common.StompMessageParser;
import asia.stampy.common.UnparseableException;
import asia.stampy.common.message.AbstractMessage;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

public class MessageParserTest {

	private StompMessageParser parser = new StompMessageParser();

	@Test
	public void testConnectMessageParsing() throws Exception {
		testEquals(new ConnectMessage("1.2", "burt.alexander"));
	}

	@Test
	public void testSendMessageParsing() throws Exception {
		SendMessage message = new SendMessage("over/there", "receiptId");
		message.setMimeType("text/plain", "UTF-8");
		message.setBody("The body");

		testEquals(message);
	}

	@Test
	public void testSendMessageParsingByteArray() throws Exception {
		SendMessage message = new SendMessage("over/there", "receiptId");
		message.setMimeType("application/jpeg", "UTF-8");
		message.setBody(new HostPort("burt.alexander", 1234));

		testEquals(message);
	}

	@Test
	public void testSubscribeMessageParsing() throws Exception {
		testEquals(new SubscribeMessage("over/there", "12345"));
	}

	@Test
	public void testConnectedMessageParsing() throws Exception {
		testEquals(new ConnectedMessage("1.2"));
	}

	@Test
	public void testErrorMessageParsing() throws Exception {
		ErrorMessage message = new ErrorMessage();
		message.setBody("The body");

		testEquals(message);
	}

	@Test
	public void testAckParsing() throws Exception {
		testEquals(new AckMessage("12345"));
	}

	@Test
	public void testNackParsing() throws Exception {
		testEquals(new NackMessage("12345"));
	}

	@Test
	public void testDisconnectParsing() throws Exception {
		testEquals(new DisconnectMessage());
	}

	@Test
	public void testUnsubscribeParsing() throws Exception {
		testEquals(new UnsubscribeMessage("12345"));
	}

	@Test
	public void testAbortParsing() throws Exception {
		testEquals(new AbortMessage("transaction"));
	}

	@Test
	public void testCommitParsing() throws Exception {
		testEquals(new CommitMessage("transaction"));
	}

	@Test
	public void testSubscribeParsing() throws Exception {
		testEquals(new SubscribeMessage("over/there", "12345"));
	}

	@Test
	public void testBeginParsing() throws Exception {
		testEquals(new BeginMessage("transaction"));
	}

	@Test
	public void testReceiptParsing() throws Exception {
		testEquals(new ReceiptMessage("12345"));
	}

	@Test
	public void testMessageParsing() throws Exception {
		MessageMessage mm = new MessageMessage("over/there", "12345", "54321");
		mm.setBody("This is the body");

		testEquals(mm);
	}

	@Test
	public void testStompParsing() throws Exception {
		testEquals(new StompMessage("burt.alexander"));
	}

	private void testEquals(AbstractMessage<?> message) throws UnparseableException {
		String stomp = message.toStompMessage(true);

		AbstractMessage<?> parsed = parser.parseMessage(stomp);

		assertTrue(message.equals(parsed));
	}
}
