package asia.stampy.common.mina;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaServiceAdapter implements IoServiceListener {

	public void serviceActivated(IoService service) throws Exception {
		// blank
	}

	public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
		// blank
	}

	public void serviceDeactivated(IoService service) throws Exception {
		// blank
	}

	public void sessionCreated(IoSession session) throws Exception {
		// blank
	}

	public void sessionDestroyed(IoSession session) throws Exception {
		// blank
	}

}
