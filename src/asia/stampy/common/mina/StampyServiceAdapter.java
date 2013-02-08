package asia.stampy.common.mina;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;

public class StampyServiceAdapter implements IoServiceListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private Map<HostPort, IoSession> sessions = new ConcurrentHashMap<>();

	public void sessionCreated(IoSession session) throws Exception {
		HostPort hostPort = createHostPort(session);
		log.info("Stampy MINA session created for {}", hostPort);
		
		sessions.put(hostPort, session);
	}

	public void sessionDestroyed(IoSession session) throws Exception {
		HostPort hostPort = createHostPort(session);
		log.info("Stampy MINA session destroyed for {}", hostPort);

		sessions.remove(hostPort);
	}

	private HostPort createHostPort(IoSession session) {
		return new HostPort((InetSocketAddress)session.getRemoteAddress());
	}
	
	public boolean hasSession(HostPort hostPort) {
		return sessions.containsKey(hostPort);
	}
	
	public IoSession getSession(HostPort hostPort) {
		IoSession session = sessions.get(hostPort);
		
		if(session == null) throw new IllegalArgumentException(hostPort.toString() + " has no current session");
		
		return session;
	}
	
	public Set<HostPort> getHostPorts() {
		return sessions.keySet();
	}
	
	public void sendMessage(String stompMessage, HostPort hostPort) {
		if(! hasSession(hostPort)) {
			return;
		}
		
		IoSession session = getSession(hostPort);
		session.write(stompMessage);
		log.debug("Sent message {} to {}", stompMessage, hostPort);
	}

	public void serviceActivated(IoService service) throws Exception {
		// blank
	}

	public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
		// blank
	}

	public void serviceDeactivated(IoService service) throws Exception {
		// blank
	}

}
