package asia.stampy.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.begin.BeginMessage;
import asia.stampy.client.message.commit.CommitMessage;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.message.AbstractBodyMessage;
import asia.stampy.common.message.AbstractBodyMessageHeader;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StampyMessageType;
import asia.stampy.common.serialization.SerializationUtils;
import asia.stampy.server.message.connected.ConnectedMessage;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

public class StompMessageParser {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String EOM = "\000";

	public <MSG extends StampyMessage<?>> MSG parseMessage(String stompMessage) throws UnparseableException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(stompMessage));

			String messageType = reader.readLine();

			StampyMessageType type = StampyMessageType.valueOf(messageType);

			List<String> headers = new ArrayList<String>();
			String hdr = reader.readLine();

			while (hdr != null && !hdr.isEmpty()) {
				headers.add(hdr);
				hdr = reader.readLine();
			}

			String body = reader.readLine();
			body = body.equals(EOM) ? null : fillBody(body, reader);

			MSG msg = createStampyMessage(type, headers);

			if (!StringUtils.isEmpty(body) && msg instanceof AbstractBodyMessage<?>) {
				AbstractBodyMessage<?> abm = (AbstractBodyMessage<?>) msg;
				abm.setBody(isText(headers) ? body : convertToObject(body, abm.getHeader().getContentType()));
			}
			return msg;
		} catch (Exception e) {
			throw new UnparseableException("The message supplied cannot be parsed as a STOMP message", stompMessage, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.warn("Could not close reader", e);
				}
			}
		}
	}

	protected Object convertToObject(String body, String contentType) throws IllegalObjectException,
			ClassNotFoundException, IOException {
		if (!AbstractBodyMessage.BINARY_BASE64_MIME_TYPE.equals(contentType)) {
			throw new NotImplementedException(
					"Subclass this class and override convertToObject to enable conversion using mime type " + contentType);
		}

		Object o = SerializationUtils.deserializeBase64(body);

		illegalObjectCheck(o);

		return o;
	}

	protected void illegalObjectCheck(Object o) throws IllegalObjectException {

	}

	protected boolean isText(List<String> headers) {
		boolean text = false;
		boolean content = false;
		for (String hdr : headers) {
			if (hdr.contains(AbstractBodyMessageHeader.CONTENT_TYPE)) {
				content = true;
				text = hdr.contains("text/");
			}
		}

		return !content || (content && text);
	}

	@SuppressWarnings("unchecked")
	protected <MSG extends StampyMessage<?>> MSG createStampyMessage(StampyMessageType type, List<String> headers)
			throws UnparseableException {

		MSG message = null;

		switch (type) {

		case ABORT:
			message = (MSG) new AbortMessage();
			break;
		case ACK:
			message = (MSG) new AckMessage();
			break;
		case BEGIN:
			message = (MSG) new BeginMessage();
			break;
		case COMMIT:
			message = (MSG) new CommitMessage();
			break;
		case CONNECT:
			message = (MSG) new ConnectMessage();
			break;
		case CONNECTED:
			message = (MSG) new ConnectedMessage();
			break;
		case DISCONNECT:
			message = (MSG) new DisconnectMessage();
			break;
		case ERROR:
			ErrorMessage error = new ErrorMessage();
			message = (MSG) error;
			break;
		case MESSAGE:
			MessageMessage mm = new MessageMessage();
			message = (MSG) mm;
			break;
		case NACK:
			message = (MSG) new NackMessage();
			break;
		case RECEIPT:
			message = (MSG) new ReceiptMessage();
			break;
		case SEND:
			SendMessage send = new SendMessage();
			message = (MSG) send;
			break;
		case STOMP:
			message = (MSG) new StompMessage();
			break;
		case SUBSCRIBE:
			message = (MSG) new SubscribeMessage();
			break;
		case UNSUBSCRIBE:
			message = (MSG) new UnsubscribeMessage();
			break;
		default:
			break;

		}
		
		message.getHeader();

		addHeaders(message, headers);

		return message;
	}

	private <MSG extends StampyMessage<?>> void addHeaders(MSG message, List<String> headers) throws UnparseableException {
		for (String header : headers) {
			StringTokenizer st = new StringTokenizer(header, ":");

			if (st.countTokens() != 2) {
				log.error("Cannot parse STOMP header {}", header);
				throw new UnparseableException("Cannot parse STOMP header " + header);
			}

			String key = st.nextToken();
			String value = st.nextToken();

			message.getHeader().addHeader(key, value);
		}
	}

	protected String fillBody(String body, BufferedReader reader) throws IOException {
		StringBuilder builder = new StringBuilder(trimEOM(body));

		String s = reader.readLine();

		while (s != null) {
			builder.append(trimEOM(s));
			s = reader.readLine();
		}

		return builder.toString();
	}

	protected String trimEOM(String s) {
		String trimmed = s;
		if (s.contains(EOM)) {
			int idx = s.indexOf(EOM);
			trimmed = s.substring(0, idx);
		}

		return trimmed;
	}
}
