package asia.stampy.common.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class AbstractMessageHeader implements StampyMessageHeader {

	private static final long serialVersionUID = 4570408820942642173L;

	public static final String CONTENT_LENGTH = "content-length";

	private Map<String, String> headers = new HashMap<>();

	public void setContentLength(int length) {
		addHeader(CONTENT_LENGTH, Integer.toString(length));
	}

	public int getContentLength() {
		String length = getHeaderValue(CONTENT_LENGTH);
		if (length == null) return -1;

		return Integer.parseInt(length);
	}

	public void addHeader(String key, String value) {
		if (!hasHeader(key)) headers.put(key, value);
	}

	public void removeHeader(String key) {
		headers.remove(key);
	}

	public String getHeaderValue(String key) {
		return headers.get(key);
	}

	public boolean hasHeader(String key) {
		return headers.containsKey(key);
	}

	@Override
	public final String toMessageHeader() {
		boolean first = true;

		StringBuilder builder = new StringBuilder();
		for (Entry<String, String> entry : headers.entrySet()) {
			if (!first) builder.append("\n");

			builder.append(entry.getKey());
			builder.append(":");
			builder.append(entry.getValue());

			first = false;
		}

		return builder.toString();
	}

	public Map<String, String> getHeaders() {
		return new HashMap<>(headers);
	}
	
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	public String toString() {
		return toMessageHeader();
	}

}
