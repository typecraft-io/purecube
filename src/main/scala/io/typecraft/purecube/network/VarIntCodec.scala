package io.typecraft.purecube.network

import io.netty.buffer.{ByteBuf, ByteBufAllocator}

object VarIntCodec {
  def decodeFrame(buf: ByteBuf): Option[ByteBuf] = {
    buf.markReaderIndex()
    val preIndex = buf.readerIndex()
    val len = readVarInt32(buf)
    if (preIndex == buf.readerIndex()) {
      return None
    }
    if (len < 0) {
      return None
    }
    if (buf.readableBytes() < len) {
      buf.resetReaderIndex()
      None
    } else {
      Some(buf.readRetainedSlice(len))
    }
  }

  def encodeFrameAlloc(
      writer: ByteBuf => Unit
  )(alloc: ByteBufAllocator): ByteBuf = {
    val data = alloc.ioBuffer()
    writer(data)
    val frame = alloc.ioBuffer()
    encodeFrame(data)(frame)
    frame
  }

  def encodeFrame(data: ByteBuf)(frame: ByteBuf): ByteBuf = {
    val bodyLen = data.readableBytes()
    val headerLen = computeRawVarInt32Size(bodyLen)
    frame.ensureWritable(headerLen + bodyLen)
    writeVarInt32(bodyLen)(frame)
    frame.writeBytes(data, data.readerIndex(), bodyLen)
    frame
  }

  def writeVarInt32(value: Int)(buf: ByteBuf): Unit = {
    var v = value
    while (true) {
      if ((v & ~0x7f) == 0) {
        buf.writeByte(v)
        return
      }
      buf.writeByte((v & 0x7f) | 0x80)
      v >>>= 7
    }
  }

  def readVarInt32(buf: ByteBuf): Int = {
    if (!buf.isReadable()) {
      return 0
    }
    buf.markReaderIndex()
    var tmp = buf.readByte()
    if (tmp >= 0) {
      return tmp
    }
    var result = tmp & 127
    if (!buf.isReadable()) {
      buf.resetReaderIndex()
      return 0
    }
    tmp = buf.readByte()
    if (tmp >= 0) {
      result |= tmp << 7
    } else {
      result |= (tmp & 127) << 7
      if (!buf.isReadable()) {
        buf.resetReaderIndex()
        return 0
      }
      tmp = buf.readByte()
      if (tmp >= 0) {
        result |= tmp << 14
      } else {
        result |= (tmp & 127) << 14
        if (!buf.isReadable()) {
          buf.resetReaderIndex()
          return 0
        }
        tmp = buf.readByte()
        if (tmp >= 0) {
          result |= tmp << 21
        } else {
          result |= (tmp & 127) << 21
          if (!buf.isReadable()) {
            buf.resetReaderIndex()
            return 0
          }
          tmp = buf.readByte()
          result |= tmp << 28
          if (tmp < 0) {
            throw new IllegalArgumentException("malformed varint.")
          }
        }
      }
    }
    result
  }

  def computeRawVarInt32Size(value: Int): Int = {
    if ((value & (0xffffffff << 7)) == 0) return 1
    if ((value & (0xffffffff << 14)) == 0) return 2
    if ((value & (0xffffffff << 21)) == 0) return 3
    if ((value & (0xffffffff << 28)) == 0) return 4
    5
  }
}
