package asia.stampy.server.mina.subscription;

public class UnexpectedAcknowledgementException extends Exception {

	private static final long serialVersionUID = 9160361992156988284L;

	public UnexpectedAcknowledgementException(String message) {
		super(message);
	}

}
