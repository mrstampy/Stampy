package asia.stampy.common.message;

public class InvalidStompMessageException extends RuntimeException {

	private static final long serialVersionUID = 4139832823187771410L;

	public InvalidStompMessageException(String message) {
		super(message);
	}

}
