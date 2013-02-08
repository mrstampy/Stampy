package asia.stampy.common;

import java.io.Serializable;
import java.net.InetSocketAddress;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class HostPort implements Serializable {
	private static final long serialVersionUID = 7989783689512750362L;
	
	private final String host;
	private final int port;

	public HostPort(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public HostPort(InetSocketAddress address) {
		this.host = address.getAddress().getHostAddress();
		this.port = address.getPort();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	public String toString() {
		return getHost() + ":" + getPort();
	}

}
