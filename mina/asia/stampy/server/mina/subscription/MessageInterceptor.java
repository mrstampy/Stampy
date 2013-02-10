package asia.stampy.server.mina.subscription;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.AbstractStampyMessageGateway;
import asia.stampy.common.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.AbstractOutgoingMessageInterceptor;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.mina.AbstractStampyMinaMessageGateway;
import asia.stampy.common.mina.MinaServiceAdapter;
import asia.stampy.server.message.message.MessageMessage;

@Resource
public class MessageInterceptor extends AbstractOutgoingMessageInterceptor {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final StompMessageType[] TYPES = { StompMessageType.MESSAGE };

	private Map<HostPort, Queue<String>> messages = new ConcurrentHashMap<>();
	
	private StampyAcknowledgementHandler handler;
	
	private Timer ackTimer = new Timer("Stampy Acknowledgement Timer", true);
	
	private long ackTimeoutMillis = 60000; 

	@Override
	public StompMessageType[] getMessageTypes() {
		return TYPES;
	}

	@Override
	public boolean isForMessage(StampyMessage<?> message) {
		MessageMessage msg = (MessageMessage)message;
		
		return StringUtils.isNotEmpty(msg.getHeader().getAck());
	}

	@Override
	public void interceptMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
		MessageMessage msg = (MessageMessage)message;
		
		String ack = msg.getHeader().getAck();
		
		Queue<String> queue = messages.get(hostPort);
		if(queue == null) {
			queue = new ConcurrentLinkedQueue<>();
			messages.put(hostPort, queue);
		}
		
		queue.add(ack);
	}

	public boolean hasMessageAck(String messageId, HostPort hostPort) {
		Queue<String> ids = messages.get(hostPort);
		if (ids == null || ids.isEmpty()) return false;

		return ids.contains(messageId);
	}

	public void clearMessageAck(String messageId, HostPort hostPort) {
		Queue<String> ids = messages.get(hostPort);
		if (ids == null) return;

		ids.remove(messageId);
	}

	public void setGateway(AbstractStampyMessageGateway gateway) {
		super.setGateway(gateway);
		((AbstractStampyMinaMessageGateway) gateway).addServiceListener(new MinaServiceAdapter() {

			public void sessionDestroyed(IoSession session) throws Exception {
				HostPort hostPort = new HostPort((InetSocketAddress) session.getRemoteAddress());
				if (messages.containsKey(hostPort)) {
					log.debug("{} session terminated, cleaning up message interceptor", hostPort);
					messages.remove(hostPort);
				}
			}
		});
	}

	public StampyAcknowledgementHandler getHandler() {
		return handler;
	}

	public void setHandler(StampyAcknowledgementHandler handler) {
		this.handler = handler;
	}

	public long getAckTimeoutMillis() {
		return ackTimeoutMillis;
	}

	public void setAckTimeoutMillis(long ackTimeoutMillis) {
		this.ackTimeoutMillis = ackTimeoutMillis;
	}

}
