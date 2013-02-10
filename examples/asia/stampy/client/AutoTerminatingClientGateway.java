package asia.stampy.client;

import asia.stampy.client.mina.ClientMinaMessageGateway;

public class AutoTerminatingClientGateway extends ClientMinaMessageGateway {

	public void shutdown() throws Exception {
		super.shutdown();
		System.exit(0);
	}

}
