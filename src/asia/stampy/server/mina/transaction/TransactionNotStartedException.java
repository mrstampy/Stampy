package asia.stampy.server.mina.transaction;

public class TransactionNotStartedException extends Exception {

	private static final long serialVersionUID = -651656641322030058L;

	public TransactionNotStartedException(String message) {
		super(message);
	}

}
