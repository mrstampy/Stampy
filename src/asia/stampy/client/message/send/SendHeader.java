package asia.stampy.client.message.send;

import asia.stampy.client.message.AbstractClientBodyMessageHeader;

public class SendHeader extends AbstractClientBodyMessageHeader {

	private static final long serialVersionUID = -4105777135779226205L;
	
	public static final String TRANSACTION = "transaction";
	public static final String DESTINATION = "destination";

	public void setDestination(String destination) {
		addHeader(DESTINATION, destination);
	}
	
	public String getDestination() {
		return getHeaderValue(DESTINATION);
	}
	
	public void setTransaction(String transaction) {
		addHeader(TRANSACTION, transaction);
	}
	
	public String getTransaction() {
		return getHeaderValue(TRANSACTION);
	}
}
