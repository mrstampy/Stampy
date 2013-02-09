package asia.stampy.server.mina.transaction;

public class TransactionAlreadyStartedException extends Exception {

	private static final long serialVersionUID = 4327171405667138826L;

	public TransactionAlreadyStartedException(String message) {
		super(message);
	}

}
