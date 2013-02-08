package asia.stampy.client.message;

import asia.stampy.common.message.AbstractBodyMessageHeader;

public class AbstractClientBodyMessageHeader extends AbstractBodyMessageHeader implements ClientMessageHeader {
	private static final long serialVersionUID = -4466902797463186691L;
	public static final String RECEIPT = "receipt";
	
	public void setReceipt(String receipt) {
		addHeader(RECEIPT, receipt);
	}
	
	public String getReceipt() {
		return getHeaderValue(RECEIPT);
	}

}
