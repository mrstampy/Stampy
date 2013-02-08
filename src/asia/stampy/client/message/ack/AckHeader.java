package asia.stampy.client.message.ack;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class AckHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = 5190052881939069233L;
	public static final String TRANSACTION = "transaction";
	public static final String ID = "id";
	
	public void setId(String id) {
		addHeader(ID, id);
	}
	
	public String getId() {
		return getHeaderValue(ID);
	}
	
	public void setTransaction(String transaction) {
		addHeader(TRANSACTION, transaction);
	}
	
	public String getTransaction() {
		return getHeaderValue(TRANSACTION);
	}

}
