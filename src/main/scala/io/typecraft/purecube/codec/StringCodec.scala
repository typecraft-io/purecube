package io.typecraft.purecube.codec

import io.netty.buffer.ByteBuf
import io.typecraft.purecube.codec.VarIntCodec.{readVarInt32, writeVarInt32}

import java.nio.charset.StandardCharsets

// TODO: NettyCodec[String]
object StringCodec {
  def readUTF(buf: ByteBuf): String = {
    val len = readVarInt32(buf)
    val bytes = Array.fill(len)(0.byteValue())
    buf.readBytes(bytes, 0, len)
    new String(bytes, StandardCharsets.UTF_8)
  }

  def writeUTF(str: String)(buf: ByteBuf): Unit = {
    writeVarInt32(str.length)(buf)
    buf.writeBytes(str.getBytes(StandardCharsets.UTF_8))
  }
}
