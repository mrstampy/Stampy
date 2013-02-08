package asia.stampy.common.message;

import java.io.Serializable;
import java.util.Map;

public interface StampyMessageHeader extends Serializable {
	
	String toMessageHeader();

	void addHeader(String key, String value);
	
	boolean hasHeader(String key);
	
	void removeHeader(String key);
	
	String getHeaderValue(String key);
	
	Map<String, String> getHeaders();
}
