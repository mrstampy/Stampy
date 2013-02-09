package asia.stampy.server.mina.transaction;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.common.mina.StampyMinaMessageListener;
import asia.stampy.server.mina.ServerMinaMessageGateway;

@Resource
public class TransactionListener implements StampyMinaMessageListener {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Queue<HostPort> activeTransactions = new ConcurrentLinkedQueue<>();
	private ServerMinaMessageGateway gateway;

	private static StompMessageType[] TYPES = { StompMessageType.ABORT, StompMessageType.BEGIN, StompMessageType.COMMIT,
			StompMessageType.DISCONNECT };

	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		return true;
	}

	@Override
	public void messageReceived(StampyMessage<?> message, IoSession session, HostPort hostPort) throws Exception {
		switch (message.getMessageType()) {
		case ABORT:
			abort(hostPort);
			break;
		case BEGIN:
			begin(hostPort);
			break;
		case COMMIT:
			commit(hostPort);
			break;
		case DISCONNECT:
			activeTransactions.remove(hostPort);
			break;
		default:
			break;

		}

	}

	private void commit(HostPort hostPort) throws TransactionNotStartedException {
		removeActiveTransaction(hostPort, "commited");
	}

	private void abort(HostPort hostPort) throws TransactionNotStartedException {
		removeActiveTransaction(hostPort, "aborted");
	}

	private void begin(HostPort hostPort) throws TransactionAlreadyStartedException {
		if (isNoTransaction(hostPort)) {
			log.info("Starting transaction for {}", hostPort);
			activeTransactions.add(hostPort);
		}
	}

	private boolean isNoTransaction(HostPort hostPort) throws TransactionAlreadyStartedException {
		if (activeTransactions.contains(hostPort)) {
			String error = "Transaction already started";
			throw new TransactionAlreadyStartedException(error);
		}

		return true;
	}

	private void removeActiveTransaction(HostPort hostPort, String function) throws TransactionNotStartedException {
		if (isTransactionStarted(hostPort)) {
			log.info("Transaction for {} {}", hostPort, function);
			activeTransactions.remove(hostPort);
		}
	}

	private boolean isTransactionStarted(HostPort hostPort) throws TransactionNotStartedException {
		if (!activeTransactions.contains(hostPort)) {
			String error = "Transaction not started";
			log.error(error);
			throw new TransactionNotStartedException(error);
		}

		return true;
	}

	public ServerMinaMessageGateway getGateway() {
		return gateway;
	}

	public void setGateway(ServerMinaMessageGateway gateway) {
		this.gateway = gateway;
		
		gateway.addServiceListener(new MinaServiceAdapter() {

			public void sessionDestroyed(IoSession session) throws Exception {
				HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
				if (activeTransactions.contains(hostPort)) {
					log.debug("{} session terminated with outstanding transaction, cleaning up", hostPort);
					activeTransactions.remove(hostPort);
				}
			}
		});
	}

}
