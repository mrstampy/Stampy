package asia.stampy.common.message.interceptor;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;

/**
 * This interface is implemented to intercept specified messages to capture the
 * state of any outgoing messages.
 * 
 * @author burton
 * 
 * @see AbstractStampyMessageGateway
 */
public interface StampyOutgoingTextInterceptor {

	/**
	 * Intercepts the outgoing message for capturing state etc.
	 * 
	 * @param message
	 * @throws InterceptException
	 *           if the outgoing message is to be aborted
	 */
	void interceptMessage(String message) throws InterceptException;

	/**
	 * Intercepts the outgoing message for capturing state etc.
	 * 
	 * @param message
	 * @throws InterceptException
	 *           if the outgoing message is to be aborted
	 */
	void interceptMessage(String message, HostPort hostPort) throws InterceptException;

}
