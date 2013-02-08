package asia.stampy.client.mina;


import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.StampyMinaHandler;

public class ClientMinaHandler extends StampyMinaHandler {

	private ClientHandlerAdapter adapter = new ClientHandlerAdapter();

	@Override
	protected boolean isValidMessage(StampyMessage<?> message) {
		return adapter.isValidMessage(message);
	}

}
