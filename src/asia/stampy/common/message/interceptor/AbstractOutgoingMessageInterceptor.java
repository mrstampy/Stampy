package asia.stampy.common.message.interceptor;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;

public abstract class AbstractOutgoingMessageInterceptor implements StampyOutgoingMessageInterceptor {

	private AbstractStampyMessageGateway gateway;

	@Override
	public void interceptMessage(StampyMessage<?> message) throws InterceptException {
		for (HostPort hostPort : getGateway().getConnectedHostPorts()) {
			interceptMessage(message, hostPort);
		}
	}

	public AbstractStampyMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(AbstractStampyMessageGateway gateway) {
		this.gateway = gateway;
	}

}
