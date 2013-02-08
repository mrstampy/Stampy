package asia.stampy.server.message.error;

import asia.stampy.common.message.AbstractBodyMessageHeader;

public class ErrorHeader extends AbstractBodyMessageHeader {

	private static final long serialVersionUID = -4679565144569363907L;
	
	public static final String MESSAGE = "message";
	public static final String RECEIPT_ID = "receipt-id";
	
	public void setReceiptId(String receiptId) {
		addHeader(RECEIPT_ID, receiptId);
	}
	
	public String getReceiptId() {
		return getHeaderValue(RECEIPT_ID);
	}
	
	public void setMessageHeader(String shortMessage) {
		addHeader(MESSAGE, shortMessage);
	}
	
	public String getMessageHeader() {
		return getHeaderValue(MESSAGE);
	}

}
