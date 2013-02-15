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
package asia.stampy.common.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.LineBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;

import asia.stampy.common.StampyLibrary;

/**
 * {@link LineBasedFrameDecoder} converted to be STOMP message aware.
 * 
 * @author burton
 * 
 */
@Sharable
@StampyLibrary(libraryName = "stampy-NETTY-client-server-RI")
public class StompBasedFrameDecoder extends FrameDecoder {

  /** Maximum length of a frame we're willing to decode. */
  private final int maxLength;
  /** Whether or not to throw an exception as soon as we exceed maxLength. */
  private final boolean failFast;

  /** True if we're discarding input because we're already over maxLength. */
  private boolean discarding;

  /**
   * Creates a new decoder.
   * 
   * @param maxLength
   *          the maximum length of the decoded frame. A
   *          {@link TooLongFrameException} is thrown if the length of the frame
   *          exceeds this value.
   */
  public StompBasedFrameDecoder(final int maxLength) {
    this(maxLength, false);
  }

  /**
   * Creates a new decoder.
   * 
   * @param maxLength
   *          the maximum length of the decoded frame. A
   *          {@link TooLongFrameException} is thrown if the length of the frame
   *          exceeds this value.
   * @param failFast
   *          If <tt>true</tt>, a {@link TooLongFrameException} is thrown as
   *          soon as the decoder notices the length of the frame will exceed
   *          <tt>maxFrameLength</tt> regardless of whether the entire frame has
   *          been read. If <tt>false</tt>, a {@link TooLongFrameException} is
   *          thrown after the entire frame that exceeds <tt>maxFrameLength</tt>
   *          has been read.
   */
  public StompBasedFrameDecoder(final int maxLength, final boolean failFast) {
    this.maxLength = maxLength;
    this.failFast = failFast;
  }

  @Override
  protected Object decode(final ChannelHandlerContext ctx, final Channel channel,
      final ChannelBuffer buffer) throws Exception {
    final int eol = findEndOfMessage(buffer);
    if (eol != -1) {
      final ChannelBuffer frame;
      final int length = eol - buffer.readerIndex();
      assert length >= 0 : "Invalid length=" + length;
      if (discarding) {
        frame = null;
        buffer.skipBytes(length);
        if (!failFast) {
          fail(ctx, "over " + (maxLength + length) + " bytes");
        }
      } else {
        int delimLength;
        final byte delim = buffer.getByte(buffer.readerIndex() + length);
        if (delim == '\r') {
          delimLength = 2; // Skip the \r\n.
        } else {
          delimLength = 1;
        }
        frame = extractFrame(buffer, buffer.readerIndex(), length + delimLength);
        buffer.skipBytes(length + delimLength);
      }
      return frame;
    }

    final int buffered = buffer.readableBytes();
    if (!discarding && buffered > maxLength) {
      discarding = true;
      if (failFast) {
        fail(ctx, buffered + " bytes buffered already");
      }
    }
    if (discarding) {
      buffer.skipBytes(buffer.readableBytes());
    }
    return null;
  }

  private void fail(final ChannelHandlerContext ctx, final String msg) {
    Channels.fireExceptionCaught(ctx.getChannel(), new TooLongFrameException("Frame length exceeds " + maxLength + " ("
        + msg + ')'));
  }

  /**
   * Returns the index in the buffer of the end of line found. Returns -1 if no
   * end of line was found in the buffer.
   */
  private static int findEndOfMessage(final ChannelBuffer buffer) {
    final int wIdx = buffer.writerIndex();
    final int rIdx = buffer.readerIndex();
    for (int i = buffer.readerIndex(); i < wIdx; i++) {
      final byte b = buffer.getByte(i);
      if (b == 0) {
        return i;
      } else if (wIdx - rIdx == 2 && b == '\r' && i < wIdx - 1 && buffer.getByte(i + 1) == '\n') {
        return i; // \r\n
      } else if (wIdx - rIdx == 1 && b == '\n') {
        return i;
      }
    }
    return -1; // Not found.
  }

}
