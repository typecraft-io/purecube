package io.typecraft.purecube.network.handshake

import io.netty.buffer.ByteBuf
import io.typecraft.purecube.network.StringCodec.readUTF
import io.typecraft.purecube.network.VarIntCodec.readVarInt32

sealed trait HandshakeServerboundPacket

case class HandshakePacket(
    protocolVersion: Int,
    serverAddress: String,
    serverPort: Int,
    nextState: Int
) extends HandshakeServerboundPacket

object HandshakePacket {
  val id: Int = 0x00

  def read(buf: ByteBuf): HandshakePacket = HandshakePacket(
    readVarInt32(buf),
    readUTF(buf),
    buf.readUnsignedShort(),
    readVarInt32(buf)
  )
}

case class PingPacket(
    millis: Long
) extends HandshakeServerboundPacket

object PingPacket {
  val id: Int = 0x01

  def read(buf: ByteBuf): PingPacket = PingPacket(
    buf.readLong()
  )
}
