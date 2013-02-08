package asia.stampy.server.message.connected;

import asia.stampy.common.message.AbstractMessageHeader;

public class ConnectedHeader extends AbstractMessageHeader {

	private static final long serialVersionUID = 1548982417648641349L;
	
	public static final String SESSION = "session";
	public static final String SERVER = "server";
	public static final String HEART_BEAT = "heart-beat";
	public static final String VERSION = "version";

	public void setVersion(String version) {
		addHeader(VERSION, version);
	}
	
	public String getVersion() {
		return getHeaderValue(VERSION);
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

	public void setServer(String server) {
		addHeader(SERVER, server);
	}
	
	public String getServer() {
		return getHeaderValue(SERVER);
	}
	
	public void setSession(String session) {
		addHeader(SESSION, session);
	}
	
	public String getSession() {
		return getHeaderValue(SESSION);
	}
	
}
