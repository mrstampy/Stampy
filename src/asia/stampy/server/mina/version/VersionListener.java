package asia.stampy.server.mina.version;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.StampyMinaMessageListener;

@Resource
public class VersionListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static StompMessageType[] TYPES = { StompMessageType.CONNECT, StompMessageType.STOMP };

	private static final String VERSION = "1.2";

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
		case CONNECT:
			checkVersion(hostPort, ((ConnectMessage) message).getHeader());
			break;
		case STOMP:
			checkVersion(hostPort, ((StompMessage) message).getHeader());
			break;
		default:
			break;

		}
	}

	private void checkVersion(HostPort hostPort, ConnectHeader header) throws VersionException {
		String acceptVersion = header.getAcceptVersion();
		
		String[] parts = acceptVersion.split(",");
		
		for(String part : parts) {
			if(part.trim().equals(VERSION)) {
				log.info("Accept version is valid for {}", hostPort);
				return;
			}
		}
		
		throw new VersionException("Only version " + VERSION + " is supported");
	}

}
