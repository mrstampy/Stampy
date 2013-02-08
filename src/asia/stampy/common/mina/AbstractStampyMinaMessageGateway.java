package asia.stampy.common.mina;

import java.util.Queue;

import asia.stampy.common.AbstractStampyMessageGateway;

public abstract class AbstractStampyMinaMessageGateway extends AbstractStampyMessageGateway {

	public abstract void addMessageListener(StampyMinaMessageListener listener);

	public abstract void removeMessageListener(StampyMinaMessageListener listener);

	public abstract void clearMessageListeners();

	public abstract void setListeners(Queue<StampyMinaMessageListener> listeners);

}
