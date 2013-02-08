package asia.stampy.client.message.connect;

import asia.stampy.common.message.AbstractMessageHeader;

public class ConnectHeader extends AbstractMessageHeader {

	private static final long serialVersionUID = 6732326426523759109L;
	
	public static final String HEART_BEAT = "heart-beat";
	public static final String PASSCODE = "passcode";
	public static final String LOGIN = "login";
	public static final String HOST = "host";
	public static final String ACCEPT_VERSION = "accept-version";

	public void setAcceptVersion(String acceptVersion) {
		addHeader(ACCEPT_VERSION, acceptVersion);
	}
	
	public String getAcceptVersion() {
		return getHeaderValue(ACCEPT_VERSION);
	}
	
	public void setHost(String host) {
		addHeader(HOST, host);
	}
	
	public String getHost() {
		return getHeaderValue(HOST);
	}
	
	public void setLogin(String user) {
		addHeader(LOGIN, user);
	}
	
	public String getLogin() {
		return getHeaderValue(LOGIN);
	}
	
	public void setPasscode(String passcode) {
		addHeader(PASSCODE, passcode);
	}
	
	public String getPasscode() {
		return getHeaderValue(PASSCODE);
	}
	
	public void setHeartbeat(int clientHBMillis, int serverHBMillis) {
		addHeader(HEART_BEAT, Integer.toString(clientHBMillis) + "," + Integer.toString(serverHBMillis));
	}
	
	public int getClientHeartbeat() {
		return getHeartbeat(0);
	}
	
	public int getServerHeartbeat() {
		return getHeartbeat(1);
	}
	
	private int getHeartbeat(int pos) {
		String hb = getHeaderValue(HEART_BEAT);
		if(hb == null) return 0;
		
		String[] parts = hb.split(",");
		
		return Integer.parseInt(parts[pos]);
	}
}
