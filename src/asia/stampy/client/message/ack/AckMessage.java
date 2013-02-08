package asia.stampy.client.message.ack;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class AckMessage extends AbstractMessage<AckHeader> {

	private static final long serialVersionUID = 1984356410866237324L;

	public AckMessage(String id) {
		this();

		getHeader().setId(id);
	}

	public AckMessage() {
		super(StampyMessageType.ACK);
	}

	@Override
	protected AckHeader createNewHeader() {
		return new AckHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getId())) {
			throw new NullPointerException(AckHeader.ID + " is required");
		}
	}

}
