package asia.stampy.server.mina.connect;

import static asia.stampy.common.message.StompMessageType.*;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.apache.mina.core.service.IoServiceListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.AbstractMinaListenerTest;

@RunWith(MockitoJUnitRunner.class)
public class ConnectListenerTest extends AbstractMinaListenerTest {
	private ConnectListener connect = new ConnectListener();

	@Before
	public void before() throws Exception {
		connect.setGateway(serverGateway);

		verify(serverGateway).addServiceListener(any(IoServiceListener.class));
		
		connect.messageReceived(getMessage(DISCONNECT), session, hostPort);
	}

	@Test
	public void testTypes() throws Exception {
		testTypes(connect, StompMessageType.values());
	}

	@Test
	public void testConnected() throws Exception {
		StompMessageType[] connectedTypes = { ABORT, ACK, BEGIN, COMMIT, NACK, SEND, SUBSCRIBE, UNSUBSCRIBE };

		for (StompMessageType type : connectedTypes) {
			try {
				connect.messageReceived(getMessage(type), session, hostPort);
				fail("Should have thrown not connected exception");
			} catch (NotConnectedException e) {
				// expected
			}
		}
		
		connect.messageReceived(getMessage(CONNECT), session, hostPort);
		
		try {
			connect.messageReceived(getMessage(CONNECT), session, hostPort);
			fail("Should have thrown not connected exception");
		} catch(AlreadyConnectedException e) {
			// expected
		}
		
		try {
			connect.messageReceived(getMessage(STOMP), session, hostPort);
			fail("Should have thrown not connected exception");
		} catch(AlreadyConnectedException e) {
			// expected
		}
	}
	
	@Test
	public void testDisconnect() throws Exception {
		connect.messageReceived(getMessage(CONNECT), session, hostPort);		
		try {
			connect.messageReceived(getMessage(CONNECT), session, hostPort);
			fail("Should have thrown not connected exception");
		} catch(AlreadyConnectedException e) {
			// expected
		}
		connect.messageReceived(getMessage(DISCONNECT), session, hostPort);
		connect.messageReceived(getMessage(CONNECT), session, hostPort);		
	}

}
