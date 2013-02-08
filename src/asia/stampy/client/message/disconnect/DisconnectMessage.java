package asia.stampy.client.message.disconnect;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class DisconnectMessage extends AbstractMessage<DisconnectHeader> {
	
	private static final long serialVersionUID = -7353329342186049989L;

	public DisconnectMessage() {
		super(StampyMessageType.DISCONNECT);
		getHeader();
	}

	@Override
	protected DisconnectHeader createNewHeader() {
		return new DisconnectHeader();
	}

	@Override
	protected void validate() {
		// TODO Auto-generated method stub

	}

}
