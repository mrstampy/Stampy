package asia.stampy.client.message.subscribe;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class SubscribeMessage extends AbstractMessage<SubscribeHeader> {

	private static final long serialVersionUID = -7008261320884282352L;

	public SubscribeMessage(String destination, String id) {
		this();

		getHeader().setDestination(destination);
		getHeader().setId(id);
	}

	public SubscribeMessage() {
		super(StampyMessageType.SUBSCRIBE);
	}

	@Override
	protected SubscribeHeader createNewHeader() {
		return new SubscribeHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getDestination())) {
			throw new NullPointerException(SubscribeHeader.DESTINATION + " is required");
		}

		if (StringUtils.isEmpty(getHeader().getId())) {
			throw new NullPointerException(SubscribeHeader.ID + " is required");
		}
	}

}
