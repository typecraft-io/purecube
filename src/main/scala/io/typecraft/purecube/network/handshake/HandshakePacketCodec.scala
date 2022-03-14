package io.typecraft.purecube.network.handshake

import io.netty.buffer.ByteBuf
import io.typecraft.purecube.network.StringCodec.{readUTF, writeUTF}
import io.typecraft.purecube.network.VarIntCodec.{readVarInt32, writeVarInt32}

object HandshakePacketCodec {
  def readHandshake(buf: ByteBuf): HandshakePacket = HandshakePacket(
    readVarInt32(buf),
    readUTF(buf),
    buf.readUnsignedShort(),
    readVarInt32(buf)
  )

  def read(buf: ByteBuf): Option[(Int, HandshakeServerboundPacket)] = {
    val id = readVarInt32(buf)
    id match {
      case HandshakePacket.id if buf.isReadable =>
        Some((id, readHandshake(buf)))
      case _ =>
        None
    }
  }
}
