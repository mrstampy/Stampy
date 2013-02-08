package asia.stampy.client.message.abort;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class AbortMessage extends AbstractMessage<AbortHeader> {

	private static final long serialVersionUID = -7511003273041211848L;

	public AbortMessage(String transaction) {
		this();

		getHeader().setTransaction(transaction);
	}

	public AbortMessage() {
		super(StampyMessageType.ABORT);
	}

	@Override
	protected AbortHeader createNewHeader() {
		return new AbortHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getTransaction())) {
			throw new NullPointerException(AbortHeader.TRANSACTION + " is required");
		}
	}

}
