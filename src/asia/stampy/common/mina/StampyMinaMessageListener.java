package asia.stampy.common.mina;

import java.util.EventListener;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;

public interface StampyMinaMessageListener extends EventListener {
	
	StampyMessageType[] getMessageTypes();
	
	boolean isForMessage(StampyMessage<?> message);
	
	void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception;
}
