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

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import asia.stampy.common.StampyLibrary;
import asia.stampy.common.mina.StampyMinaHandler;

/**
 * The Class StringEncoder.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class StringEncoder extends ProtocolEncoderAdapter {

  private int maxDataLength = Integer.MAX_VALUE;

  /**
   * Instantiates a new string encoder.
   * 
   * @param maxDataLength
   *          the max data length
   */
  public StringEncoder(int maxDataLength) {
    super();
    setMaxDataLength(maxDataLength);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core
   * .session.IoSession, java.lang.Object,
   * org.apache.mina.filter.codec.ProtocolEncoderOutput)
   */
  @Override
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
