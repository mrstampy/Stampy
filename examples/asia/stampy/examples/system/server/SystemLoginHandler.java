package asia.stampy.examples.system.server;

import asia.stampy.server.mina.login.NotLoggedInException;
import asia.stampy.server.mina.login.StampyLoginHandler;
import asia.stampy.server.mina.login.TerminateSessionException;

public class SystemLoginHandler implements StampyLoginHandler {

	public static final String SEE_THE_SYSTEM_ADMINISTRATOR = "See the system administrator";
	public static final String GOOD_USER = "gooduser";
	public static final String BAD_USER = "baduser";
	
	private int maxFailedLoginAttempts = 3;
	
	private int failedLoginAttempts = 0;
	
	@Override
	public void login(String username, String password) throws NotLoggedInException, TerminateSessionException {
		if(GOOD_USER.equals(username)) return;
		
		failedLoginAttempts++;
		
		if(failedLoginAttempts >= getMaxFailedLoginAttempts()) {
			throw new TerminateSessionException(SEE_THE_SYSTEM_ADMINISTRATOR);
		}
		
		throw new NotLoggedInException("Username " + username + " cannot be logged in");
	}

	public int getMaxFailedLoginAttempts() {
		return maxFailedLoginAttempts;
	}

	public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
		this.maxFailedLoginAttempts = maxFailedLoginAttempts;
	}

}
