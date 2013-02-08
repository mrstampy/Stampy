package asia.stampy.server.message.receipt;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class ReceiptMessage extends AbstractMessage<ReceiptHeader> {
	
	private static final long serialVersionUID = -5942932500390572224L;

	public ReceiptMessage(String receiptId) {
		this();
		
		getHeader().setReceiptId(receiptId);
	}

	public ReceiptMessage() {
		super(StampyMessageType.RECEIPT);
	}

	@Override
	protected ReceiptHeader createNewHeader() {
		return new ReceiptHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getReceiptId())) {
			throw new NullPointerException(ReceiptHeader.RECEIPT_ID + " is required");
		}
	}

}
