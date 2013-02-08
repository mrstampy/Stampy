package asia.stampy.common.message;

import java.io.Serializable;

public interface StampyMessage<HDR extends StampyMessageHeader> extends Serializable {

	HDR getHeader();

	String toStompMessage(boolean validate);
	
	StampyMessageType getMessageType();

}
