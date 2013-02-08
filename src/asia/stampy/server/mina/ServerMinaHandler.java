package asia.stampy.server.mina;

import org.apache.mina.core.session.IoSession;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.StampyMinaHandler;

public class ServerMinaHandler extends StampyMinaHandler {

	private ServerHandlerAdapter adapter = new ServerHandlerAdapter();

	@Override
	protected boolean isValidMessage(StampyMessage<?> message) {
		return adapter.isValidMessage(message);
	}

	protected void errorHandle(StampyMessage<?> message, Exception e, IoSession session, HostPort hostPort) {
		adapter.errorHandle(message, e, hostPort);
	}

	protected void sendResponseIfRequired(StampyMessage<?> message, IoSession session, HostPort hostPort) {
		adapter.sendResponseIfRequired(message, session, hostPort);
	}

	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		super.setMessageGateway(messageGateway);
		adapter.setMessageGateway(messageGateway);
	}

}
