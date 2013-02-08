package asia.stampy.client.mina;


import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.mina.raw.StampyRawStringHandler;

public class RawClientMinaHandler extends StampyRawStringHandler {

	private ClientHandlerAdapter adapter = new ClientHandlerAdapter();

	@Override
	protected boolean isValidMessage(StampyMessage<?> message) {
		return adapter.isValidMessage(message);
	}

}
