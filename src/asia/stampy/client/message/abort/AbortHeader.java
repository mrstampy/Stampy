package asia.stampy.client.message.abort;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class AbortHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = 5682449671872257059L;
	public static final String TRANSACTION = "transaction";
	
	public void setTransaction(String transaction) {
		addHeader(TRANSACTION, transaction);
	}
	
	public String getTransaction() {
		return getHeaderValue(TRANSACTION);
	}

}
