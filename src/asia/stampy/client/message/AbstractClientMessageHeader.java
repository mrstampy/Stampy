package asia.stampy.client.message;

import asia.stampy.common.message.AbstractMessageHeader;

public class AbstractClientMessageHeader extends AbstractMessageHeader implements ClientMessageHeader {
	private static final long serialVersionUID = -6352998102776340557L;
	public static final String RECEIPT = "receipt";
	
	public void setReceipt(String receipt) {
		addHeader(RECEIPT, receipt);
	}
	
	public String getReceipt() {
		return getHeaderValue(RECEIPT);
	}

}
