package asia.stampy.client.message.begin;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class BeginHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = 1752296477013796007L;
	public static final String TRANSACTION = "transaction";
	
	public void setTransaction(String transaction) {
		addHeader(TRANSACTION, transaction);
	}
	
	public String getTransaction() {
		return getHeaderValue(TRANSACTION);
	}

}
