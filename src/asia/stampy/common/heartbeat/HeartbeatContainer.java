package asia.stampy.common.heartbeat;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;

public class HeartbeatContainer {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Map<HostPort, PaceMaker> paceMakers = new ConcurrentHashMap<>();

	public void stop(HostPort hostPort) {
		PaceMaker paceMaker = paceMakers.get(hostPort);
		if (paceMaker != null) {
			log.info("Stopping PaceMaker for {}", hostPort);
			paceMaker.stop();
		}
	}
	
	public void add(HostPort hostPort, PaceMaker paceMaker) {
		log.info("Adding PaceMaker for {}", hostPort);
		stop(hostPort);
		paceMakers.put(hostPort, paceMaker);
	}
	
	public void remove(HostPort hostPort) {
		log.info("Removing PaceMaker for {}", hostPort);
		stop(hostPort);
		paceMakers.remove(hostPort);
	}
	
	public void reset(HostPort hostPort) {
		log.trace("Resetting PaceMaker for {}", hostPort);
		PaceMaker paceMaker = paceMakers.get(hostPort);
		if(paceMaker != null) paceMaker.reset();
	}
}
