package asia.stampy.common.message;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asia.stampy.common.serialization.SerializationUtils;

public abstract class AbstractBodyMessage<HDR extends AbstractBodyMessageHeader> extends AbstractMessage<HDR> {
	private static final long serialVersionUID = 3988865546656906553L;

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String BINARY_BASE64_MIME_TYPE = "binary/base64";

	private String bodyEncoding = BINARY_BASE64_MIME_TYPE;

	private Object body;

	protected AbstractBodyMessage(StampyMessageType messageType) {
		super(messageType);
	}

	@SuppressWarnings("unchecked")
	public <O extends Object> O getBody() {
		return (O) body;
	}

	public <O extends Object> void setBody(O body) {
		this.body = body;
	}

	public void setMimeType(String mimeType) {
		getHeader().setContentType(mimeType);
	}

	public void setMimeType(String mimeType, String encoding) {
		mimeType += ";charset=" + encoding;
		setMimeType(mimeType);
	}

	public boolean isText() {
		String value = getHeader().getContentType();
		if (value == null) return true;

		return value.contains("text/");
	}

	protected String postHeader() {
		if (getBody() == null) return null;

		if (isText()) {
			return getBody();
		} else {
			try {
				String encoded = getBodyEncoding().equals(BINARY_BASE64_MIME_TYPE) ? getObjectArrayAsBase64(getBody())
						: getObjectArrayAsString(getBody());
				getHeader().removeHeader(AbstractBodyMessageHeader.CONTENT_TYPE);
				getHeader().removeHeader(AbstractBodyMessageHeader.CONTENT_LENGTH);
				getHeader().setContentLength(encoded.length());
				getHeader().setContentType(getBodyEncoding());
				return encoded;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected String getObjectArrayAsString(Object body) {
		throw new NotImplementedException("Subclass the abstract body message and override getObjectArrayAsString for "
				+ getBodyEncoding() + " encoding");
	}

	public String getObjectArrayAsBase64(Object o) throws IOException {
		log.debug("Serializing object {} to a string", o);
		return SerializationUtils.serializeBase64(o);
	}

	public String getBodyEncoding() {
		return bodyEncoding;
	}

	public void setBodyEncoding(String bodyEncoding) {
		this.bodyEncoding = bodyEncoding;
	}

}
