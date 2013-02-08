package asia.stampy.server.message.connected;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class ConnectedMessage extends AbstractMessage<ConnectedHeader> {

	private static final long serialVersionUID = -7120496085646311030L;

	public ConnectedMessage(String version) {
		this();
		getHeader().setVersion(version);
	}

	public ConnectedMessage() {
		super(StampyMessageType.CONNECTED);
	}

	@Override
	protected ConnectedHeader createNewHeader() {
		return new ConnectedHeader();
	}

	@Override
	protected void validate() {
		if (StringUtils.isEmpty(getHeader().getVersion())) {
			throw new NullPointerException(ConnectedHeader.VERSION + " is required");
		}

	}

}
