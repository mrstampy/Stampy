package asia.stampy.client.message.nack;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class NackMessage extends AbstractMessage<NackHeader> {

	private static final long serialVersionUID = -1213685585376641464L;

	public NackMessage(String id) {
		this();
		
		getHeader().setId(id);
	}
	
	public NackMessage() {
		super(StampyMessageType.NACK);
	}

	@Override
	protected NackHeader createNewHeader() {
		return new NackHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getId())) {
			throw new NullPointerException(NackHeader.ID + " is required");
		}
	}

}
