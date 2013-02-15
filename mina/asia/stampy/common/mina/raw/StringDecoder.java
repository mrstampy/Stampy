/*
 * Copyright (C) 2013 Burton Alexander
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 */
package asia.stampy.common.mina.raw;

import java.io.InputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.mina.StampyMinaHandler;

/**
 * The Class StringDecoder.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class StringDecoder extends CumulativeProtocolDecoder {

  private int maxDataLength = Integer.MAX_VALUE;

  /**
   * Instantiates a new string decoder.
   * 
   * @param maxDataLength
   *          the max data length
   */
  public StringDecoder(int maxDataLength) {
    super();
    setMaxDataLength(maxDataLength);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache
   * .mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer,
   * org.apache.mina.filter.codec.ProtocolDecoderOutput)
   */
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

  /**
   * Gets the max data length.
   * 
   * @return the max data length
   */
  public int getMaxDataLength() {
    return maxDataLength;
  }

  /**
   * Sets the max data length.
   * 
   * @param maxDataLength
   *          the new max data length
   */
  public void setMaxDataLength(int maxDataLength) {
    this.maxDataLength = maxDataLength;
  }

}
