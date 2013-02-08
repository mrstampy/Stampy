package asia.stampy.client.message.connect;

import org.apache.commons.lang.StringUtils;

import asia.stampy.common.message.AbstractMessage;
import asia.stampy.common.message.StampyMessageType;

public class ConnectMessage extends AbstractMessage<ConnectHeader> {

	private static final long serialVersionUID = 1164477258648698915L;

	public ConnectMessage(String acceptVersion, String host) {
		this();

		getHeader().setAcceptVersion(acceptVersion);
		getHeader().setHost(host);
	}
	
	public ConnectMessage(String host) {
		this("1.2", host);
	}
	
	public ConnectMessage() {
		super(StampyMessageType.CONNECT);
	}

	@Override
	protected ConnectHeader createNewHeader() {
		return new ConnectHeader();
	}

	@Override
	protected void validate() {
		if(StringUtils.isEmpty(getHeader().getAcceptVersion())) {
			throw new NullPointerException(ConnectHeader.ACCEPT_VERSION + " is required");
		}
		
		if(StringUtils.isEmpty(getHeader().getHost())) {
			throw new NullPointerException(ConnectHeader.HOST + " is required");
		}
	}

}
