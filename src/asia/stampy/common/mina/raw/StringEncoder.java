package asia.stampy.common.mina.raw;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import asia.stampy.common.mina.StampyMinaHandler;

public class StringEncoder extends ProtocolEncoderAdapter {

	private int maxDataLength = Integer.MAX_VALUE;

	public StringEncoder(int maxDataLength) {
		super();
		setMaxDataLength(maxDataLength);
	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		String value = (String) message;
		IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
		buf.putString(value, StampyMinaHandler.CHARSET.newEncoder());
		if (buf.position() > getMaxDataLength()) {
			throw new IllegalArgumentException("Data length: " + buf.position());
		}
		buf.flip();
		out.write(buf);
	}

	public int getMaxDataLength() {
		return maxDataLength;
	}

	public void setMaxDataLength(int maxDataLength) {
		this.maxDataLength = maxDataLength;
	}

}
