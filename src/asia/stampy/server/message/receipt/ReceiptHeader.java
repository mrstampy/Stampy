package asia.stampy.server.message.receipt;

import asia.stampy.common.message.AbstractMessageHeader;

public class ReceiptHeader extends AbstractMessageHeader {
	private static final long serialVersionUID = 2499933932635661316L;
	
	public static final String RECEIPT_ID = "receipt-id";
	
	public void setReceiptId(String receiptId) {
		addHeader(RECEIPT_ID, receiptId);
	}
	
	public String getReceiptId() {
		return getHeaderValue(RECEIPT_ID);
	}

}
