package asia.stampy.client.message.send;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.StampyMessageType;

public class SendMessage extends AbstractBodyMessage<SendHeader> {

	private static final long serialVersionUID = -104251665180607773L;

	public SendMessage(String destination, String receipt) {
		this();

		getHeader().setDestination(destination);
		getHeader().setReceipt(receipt);
	}

	public SendMessage() {
		super(StampyMessageType.SEND);
	}

	@Override
	protected SendHeader createNewHeader() {
		return new SendHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getDestination())) {
			throw new NullPointerException(SendHeader.DESTINATION + " is required");
		}
	}

}
