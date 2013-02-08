package asia.stampy.common.mina.raw;

import java.io.InputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import asia.stampy.common.mina.StampyMinaHandler;

public class StringDecoder extends CumulativeProtocolDecoder {

	private int maxDataLength = Integer.MAX_VALUE;

	public StringDecoder(int maxDataLength) {
		super();
		setMaxDataLength(maxDataLength);
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (!in.hasRemaining()) return false;

		InputStream is = in.asInputStream();
		int available = is.available();
		if (available > getMaxDataLength()) {
			throw new IllegalArgumentException("Data length: " + available);
		}

		byte[] b = new byte[available];
		is.read(b);
		String decoded = new String(b, StampyMinaHandler.CHARSET).replace("\nnull", "\n");
		out.write(decoded);
		return true;
	}

	public int getMaxDataLength() {
		return maxDataLength;
	}

	public void setMaxDataLength(int maxDataLength) {
		this.maxDataLength = maxDataLength;
	}

}
