package asia.stampy.common.message.interceptor;

public class InterceptException extends Exception {

	private static final long serialVersionUID = 3708895403125259300L;

	public InterceptException(String message) {
		super(message);
	}

	public InterceptException(String message, Throwable cause) {
		super(message, cause);
	}

}
