package asia.stampy.common;

import asia.stampy.common.message.StampyMessage;

public abstract class AbstractStampyMessageGateway {
	
	public void sendMessage(StampyMessage<?> message, HostPort hostPort) {
		sendMessage(message.toStompMessage(true), hostPort);
	}
	
	public void broadcastMessage(StampyMessage<?> message) {
		broadcastMessage(message.toStompMessage(true));
	}
	
	public abstract void broadcastMessage(String stompMessage);

	public abstract void sendMessage(String stompMessage, HostPort hostPort);
	
	public abstract void closeConnection(HostPort hostPort);
	
	public abstract void connect() throws Exception;
	
	public abstract void shutdown() throws Exception;
	
	public abstract boolean isConnected(HostPort hostPort);
}
