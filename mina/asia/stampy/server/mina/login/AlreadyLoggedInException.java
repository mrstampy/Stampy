package asia.stampy.server.mina.login;

public class AlreadyLoggedInException extends Exception {

	private static final long serialVersionUID = 4175613077223909784L;

	public AlreadyLoggedInException(String message) {
		super(message);
	}

}
