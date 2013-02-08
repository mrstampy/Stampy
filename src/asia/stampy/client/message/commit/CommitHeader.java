package asia.stampy.client.message.commit;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class CommitHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = 503530501264860164L;
	public static final String TRANSACTION = "transaction";
	
	public void setTransaction(String transaction) {
		addHeader(TRANSACTION, transaction);
	}
	
	public String getTransaction() {
		return getHeaderValue(TRANSACTION);
	}

}
