package asia.stampy.server.message.message;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.StampyMessageType;

public class MessageMessage extends AbstractBodyMessage<MessageHeader> {

	private static final long serialVersionUID = 5351072786156865214L;

	public MessageMessage(String destination, String messageId, String subscription) {
		this();
		
		getHeader().setDestination(destination);
		getHeader().setMessageId(messageId);
		getHeader().setSubscription(subscription);
	}
	
	public MessageMessage() {
		super(StampyMessageType.MESSAGE);
	}

	@Override
	protected MessageHeader createNewHeader() {
		return new MessageHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getDestination())) {
			throw new NullPointerException(MessageHeader.DESTINATION + " is required");
		}
		
		if (StringUtils.isEmpty(getHeader().getMessageId())) {
			throw new NullPointerException(MessageHeader.MESSAGE_ID + " is required");
		}
		
		if (StringUtils.isEmpty(getHeader().getSubscription())) {
			throw new NullPointerException(MessageHeader.SUBSCRIPTION + " is required");
		}
	}

}
