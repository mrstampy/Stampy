package asia.stampy.client.message.commit;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class CommitMessage extends AbstractMessage<CommitHeader> {

	private static final long serialVersionUID = 3916270310464057075L;

	public CommitMessage(String transaction) {
		this();

		getHeader().setTransaction(transaction);
	}

	public CommitMessage() {
		super(StampyMessageType.COMMIT);
	}

	@Override
	protected CommitHeader createNewHeader() {
		return new CommitHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getTransaction())) {
			throw new NullPointerException(CommitHeader.TRANSACTION + " is required");
		}
	}

}
