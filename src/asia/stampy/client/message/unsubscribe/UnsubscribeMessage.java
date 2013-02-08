package asia.stampy.client.message.unsubscribe;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class UnsubscribeMessage extends AbstractMessage<UnsubscribeHeader> {

	private static final long serialVersionUID = -4889351559871669847L;

	public UnsubscribeMessage(String id) {
		this();

		getHeader().setId(id);
	}

	public UnsubscribeMessage() {
		super(StampyMessageType.UNSUBSCRIBE);
	}

	@Override
	protected UnsubscribeHeader createNewHeader() {
		return new UnsubscribeHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getId())) {
			throw new NullPointerException(UnsubscribeHeader.ID + " is required");
		}
	}

}
