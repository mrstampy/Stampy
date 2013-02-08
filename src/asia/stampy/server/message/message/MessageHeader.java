package asia.stampy.server.message.message;

import asia.stampy.common.message.AbstractBodyMessageHeader;

public class MessageHeader extends AbstractBodyMessageHeader {

	private static final long serialVersionUID = -1715758376656092863L;
	
	public static final String ACK = "ack";
	public static final String SUBSCRIPTION = "subscription";
	public static final String MESSAGE_ID = "message-id";
	public static final String DESTINATION = "destination";

	public void setDestination(String destination) {
		addHeader(DESTINATION, destination);
	}
	
	public String getDestination() {
		return getHeaderValue(DESTINATION);
	}
	
	public void setMessageId(String messageId) {
		addHeader(MESSAGE_ID, messageId);
	}
	
	public String getMessageId() {
		return getHeaderValue(MESSAGE_ID);
	}
	
	public void setSubscription(String subscription) {
		addHeader(SUBSCRIPTION, subscription);
	}
	
	public String getSubscription() {
		return getHeaderValue(SUBSCRIPTION);
	}
	
	public void setAck() {
		addHeader(ACK, Boolean.TRUE.toString());
	}
	
	public String getAck() {
		return getHeaderValue(ACK);
	}
}
