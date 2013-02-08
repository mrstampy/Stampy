package asia.stampy.common.heartbeat;

import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;

public class PaceMaker {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private long timeInMillis;
	private TimerTask stopwatch;
	private Timer timer = new Timer("PaceMaker", true);

	private AbstractStampyMessageGateway messageGateway;

	private HostPort hostPort;

	private int heartbeatCount;

	public static final String HB1 = "\n";
	public static final String HB2 = "\r";

	public PaceMaker(int timeInMillis) {
		this.timeInMillis = timeInMillis;
		start();
	}

	public void reset() {
		log.trace("PaceMaker reset invoked");
		setHeartbeatCount(0);
		stop();
		start();
	}

	public void stop() {
		log.trace("PaceMaker stop invoked");
		if (stopwatch != null) stopwatch.cancel();
	}

	public void start() {
		log.trace("PaceMaker start invoked for sleep time of {} ms", getSleepTime());
		stopwatch = new TimerTask() {

			@Override
			public void run() {
				executeHeartbeat();
			}
		};

		timer.schedule(stopwatch, getSleepTime());
	}

	private void executeHeartbeat() {
		if (heartbeatCount >= 2) {
			log.warn("No response after 2 heartbeats, closing connection");
			messageGateway.closeConnection(getHostPort());
		} else {
			messageGateway.sendMessage(HB1, getHostPort());
			log.debug("Sent heartbeat");
			start();
			heartbeatCount++;
		}
	}

	public long getSleepTime() {
		return timeInMillis;
	}

	public AbstractStampyMessageGateway getMessageGateway() {
		return messageGateway;
	}

	public void setMessageGateway(AbstractStampyMessageGateway messageGateway) {
		this.messageGateway = messageGateway;
	}

	public HostPort getHostPort() {
		return hostPort;
	}

	public void setHostPort(HostPort hostPort) {
		this.hostPort = hostPort;
	}

	public int getHeartbeatCount() {
		return heartbeatCount;
	}

	public void setHeartbeatCount(int heartbeatCount) {
		this.heartbeatCount = heartbeatCount;
	}

}
