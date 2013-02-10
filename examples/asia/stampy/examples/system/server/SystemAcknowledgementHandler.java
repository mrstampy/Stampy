package asia.stampy.examples.system.server;

import asia.stampy.server.mina.subscription.StampyAcknowledgementHandler;

public class SystemAcknowledgementHandler implements StampyAcknowledgementHandler {

	@Override
	public void ackReceived(String id, String receipt, String transaction) throws Exception {

	}

	@Override
	public void nackReceived(String id, String receipt, String transaction) throws Exception {


	}

}
