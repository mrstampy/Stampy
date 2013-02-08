package asia.stampy.server.message.error;

import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.StampyMessageType;

public class ErrorMessage extends AbstractBodyMessage<ErrorHeader> {

	private static final long serialVersionUID = -4583369848020945035L;

	public ErrorMessage() {
		super(StampyMessageType.ERROR);
	}
	
	public ErrorMessage(String receiptId) {
		this();
		
		getHeader().setReceiptId(receiptId);
	}

	@Override
	protected ErrorHeader createNewHeader() {
		return new ErrorHeader();
	}

	@Override
	protected void validate() {
		// TODO Auto-generated method stub

	}

}
