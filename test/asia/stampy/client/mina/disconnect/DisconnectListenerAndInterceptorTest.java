package asia.stampy.client.mina.disconnect;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.AbstractMinaListenerTest;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.message.receipt.ReceiptMessage;

@RunWith(MockitoJUnitRunner.class)
public class DisconnectListenerAndInterceptorTest extends AbstractMinaListenerTest {

	private DisconnectListenerAndInterceptor disconnect = new DisconnectListenerAndInterceptor();

	@After
	public void after() throws Exception {
		disconnect.setReceiptId(null);
	}

	@Test
	public void testTypes() throws Exception {
		testTypes((StampyMinaMessageListener) disconnect, new StompMessageType[] { StompMessageType.DISCONNECT,
				StompMessageType.RECEIPT });
	}

	@Test
	public void testIsForMessage() throws Exception {
		DisconnectMessage message = new DisconnectMessage();
		assertFalse(disconnect.isForMessage(message));

		message.getHeader().setReceipt("blah");
		assertTrue(disconnect.isForMessage(message));

		ReceiptMessage receipt = new ReceiptMessage("blah");
		assertFalse(disconnect.isForMessage(receipt));

		disconnect.interceptMessage(message, hostPort);
		assertTrue(disconnect.isForMessage(receipt));
	}
	
	@Test
	public void testMessageReceived() throws Exception {
		disconnect.setReceiptId("test");
		
		ReceiptMessage message = new ReceiptMessage("blah");
				
		disconnect.setCloseOnDisconnectMessage(false);
		disconnect.messageReceived(message, session, hostPort);
		
		verify(session, never()).close(false);
		
		disconnect.setReceiptId("test");
		disconnect.setCloseOnDisconnectMessage(true);
		
		disconnect.messageReceived(message, session, hostPort);
		
		verify(session).close(false);
	}

}
