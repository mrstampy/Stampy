package asia.stampy.server.mina.login;

public interface StampyLoginHandler {
	
	void login(String username, String password) throws NotLoggedInException, TerminateSessionException;

}
