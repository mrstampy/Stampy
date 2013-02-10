package asia.stampy.server.mina.subscription;

public interface StampyAcknowledgementHandler {
	
	void ackReceived(String id, String receipt, String transaction) throws Exception;
	
	void nackReceived(String id, String receipt, String transaction) throws Exception;

}
