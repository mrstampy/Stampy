package asia.stampy.client.message.stomp;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class StompMessage extends AbstractMessage<StompHeader> {

	private static final long serialVersionUID = 4889982516009469738L;

	public StompMessage(String host) {
		this();

		getHeader().setAcceptVersion("1.2");
		getHeader().setHost(host);
	}
	
	public StompMessage() {
		super(StampyMessageType.STOMP);
	}

	@Override
	protected StompHeader createNewHeader() {
		return new StompHeader();
	}

	@Override
	protected void validate() {
		// TODO Auto-generated method stub

	}

}
