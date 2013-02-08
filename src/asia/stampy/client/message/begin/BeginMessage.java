package asia.stampy.client.message.begin;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class BeginMessage extends AbstractMessage<BeginHeader> {

	private static final long serialVersionUID = -1331929421949592240L;

	public BeginMessage(String transaction) {
		this();
		
		getHeader().setTransaction(transaction);
	}
	
	public BeginMessage() {
		super(StampyMessageType.BEGIN);
	}

	@Override
	protected BeginHeader createNewHeader() {
		return new BeginHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getTransaction())) {
			throw new NullPointerException(BeginHeader.TRANSACTION + " is required");
		}
	}

}
