package asia.stampy.client.message.unsubscribe;

import asia.stampy.client.message.AbstractClientMessageHeader;

public class UnsubscribeHeader extends AbstractClientMessageHeader {
	private static final long serialVersionUID = -6205303835381181615L;
	
	public static final String ID = "id";

	public void setId(String id) {
		addHeader(ID, id);
	}
	
	public String getId() {
		return getHeaderValue(ID);
	}

}
