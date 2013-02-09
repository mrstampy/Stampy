package asia.stampy.common.message.interceptor;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

/**
 * This interface is implemented to intercept specified messages to capture the
 * state of any outgoing messages.
 * 
 * @author burton
 * 
 * @see AbstractStampyMessageGateway
 */
public interface StampyOutgoingMessageInterceptor {

	/**
	 * Gets the message types of which the implementation is interested.
	 * 
	 * @return the message types
	 */
	StompMessageType[] getMessageTypes();

	/**
	 * Returns true if the message should be processed by the implementation.
	 * 
	 * @param message
	 *          the message
	 * @return true, if is for message
	 */
	boolean isForMessage(StampyMessage<?> message);

	/**
	 * Intercepts the outgoing message for capturing state etc.
	 * 
	 * @param message
	 * @throws InterceptException
	 *           if the outgoing message is to be aborted
	 */
	void interceptMessage(StampyMessage<?> message) throws InterceptException;

}
