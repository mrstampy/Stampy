package asia.stampy.common.message.interceptor;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;

public abstract class AbstractOutgoingTextInterceptor implements StampyOutgoingTextInterceptor {

	private AbstractStampyMessageGateway gateway;

	@Override
	public void interceptMessage(String message) throws InterceptException {
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
