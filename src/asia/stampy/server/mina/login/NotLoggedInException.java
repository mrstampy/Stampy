package asia.stampy.server.mina.login;

public class NotLoggedInException extends Exception {

	private static final long serialVersionUID = 6115947456180110688L;

	public NotLoggedInException(String message) {
		super(message);
	}

}
