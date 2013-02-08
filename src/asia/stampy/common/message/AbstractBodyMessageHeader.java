package asia.stampy.common.message;

public class AbstractBodyMessageHeader extends AbstractMessageHeader {

	private static final long serialVersionUID = -4546439038775868974L;
	
	public static final String CONTENT_TYPE = "content-type";

	public void setContentType(String mimeType) {
		addHeader(CONTENT_TYPE, mimeType);
	}
	
	public String getContentType() {
		return getHeaderValue(CONTENT_TYPE);
	}
}
