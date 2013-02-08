package asia.stampy.common.message;

public enum StampyMessageType {

	CONNECT,
	STOMP,
	CONNECTED,
	SEND(true), 
	SUBSCRIBE, 
	UNSUBSCRIBE, 
	ACK, 
	NACK, 
	BEGIN, 
	COMMIT, 
	ABORT, 
	DISCONNECT, 
	MESSAGE(true), 
	RECEIPT, 
	ERROR(true);
	
	boolean hasBody;
	
	StampyMessageType() {
		this(false);
	}
	
	StampyMessageType(boolean hasBody) {
		this.hasBody = hasBody;
	}
	
	public boolean hasBody() {
		return hasBody;
	}

}
