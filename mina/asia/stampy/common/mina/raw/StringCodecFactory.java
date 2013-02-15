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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import asia.stampy.common.StampyLibrary;

/**
 * A codec factory to process raw (non-MINA terminated) strings.
 */
@StampyLibrary(libraryName = "stampy-MINA-client-server-RI")
public class StringCodecFactory implements ProtocolCodecFactory {
  private ProtocolEncoder encoder;
  private ProtocolDecoder decoder;

  /**
   * Instantiates a new string codec factory.
   * 
   * @param maxSize
   *          the max size
   */
  public StringCodecFactory(int maxSize) {
    encoder = new StringEncoder(maxSize);
    decoder = new StringDecoder(maxSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.ProtocolCodecFactory#getEncoder(org.apache
   * .mina.core.session.IoSession)
   */
  @Override
  public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    return encoder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.mina.filter.codec.ProtocolCodecFactory#getDecoder(org.apache
   * .mina.core.session.IoSession)
   */
  @Override
  public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    return decoder;
  }

}
