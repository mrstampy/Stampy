package asia.stampy.common.message;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import asia.stampy.common.StompMessageParser;

public abstract class AbstractMessage<HDR extends StampyMessageHeader> implements StampyMessage<HDR> {

	private static final long serialVersionUID = -577180637937320507L;

	private HDR header;
	private final StampyMessageType messageType;

	protected AbstractMessage(StampyMessageType messageType) {
		this.messageType = messageType;
	}

	public HDR getHeader() {
		if (header == null) header = createNewHeader();
		return header;
	}

	protected abstract HDR createNewHeader();

	public StampyMessageType getMessageType() {
		return messageType;
	}

	public final String toStompMessage(boolean validate) {
		if (validate) validate();

		StringBuilder builder = new StringBuilder();

		String body = postHeader();

		builder.append(getMessageType().name());
		String header = getHeader().toMessageHeader();
		if (StringUtils.isNotEmpty(header)) {
			builder.append("\n");
			builder.append(header);
		}
		builder.append("\n\n");
		builder.append(body);

		builder.append(StompMessageParser.EOM);

		return builder.toString();
	}

	protected abstract void validate();

	protected String postHeader() {
		return null;
	}

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public String toString() {
		return toStompMessage(false);
	}

}
