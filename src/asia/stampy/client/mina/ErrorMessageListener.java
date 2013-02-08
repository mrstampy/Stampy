package asia.stampy.client.mina;

import java.lang.invoke.MethodHandles;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

public class ErrorMessageListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public StampyMessageType[] getMessageTypes() {
		return new StampyMessageType[] { StampyMessageType.ERROR };
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		log.error("Received ERROR message {} from {}", message, hostPort);
	}

}
