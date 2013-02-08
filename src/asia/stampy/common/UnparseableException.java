package asia.stampy.common;

public class UnparseableException extends Exception {
	private static final long serialVersionUID = -5077635019985663697L;
	
	private String stompMessage;

	public UnparseableException(String message, String stompMessage, Throwable cause) {
		super(message, cause);
		this.stompMessage = stompMessage;
	}
	
	public UnparseableException(String message) {
		super(message);
	}

	public String getStompMessage() {
		return stompMessage;
	}

}
