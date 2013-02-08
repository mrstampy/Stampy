package asia.stampy.client.mina;

import asia.stampy.common.message.StampyMessage;

public class ClientHandlerAdapter {
	
	boolean isValidMessage(StampyMessage<?> message) {
		switch(message.getMessageType()) {
		
		case CONNECTED:
		case ERROR:
		case MESSAGE:
		case RECEIPT:
			return true;
		default:
			return false;
		
		}
	}

}
