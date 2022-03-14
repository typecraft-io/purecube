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

  def readPing(buf: ByteBuf): PingPacket = PingPacket(
    buf.readLong()
  )

  def read(buf: ByteBuf): Option[(Int, HandshakeServerboundPacket)] = {
    val id = readVarInt32(buf)
    id match {
      case HandshakePacket.id =>
        Some((id, readHandshake(buf)))
      case PingPacket.id =>
        Some((id, readPing(buf)))
      case _ =>
        None
    }
  }

  def writeResponse(buf: ByteBuf)(response: ResponsePacket): Unit = {
    writeUTF(response.json)(buf)
  }

  def writePong(buf: ByteBuf)(pong: PongPacket): Unit = {
    buf.writeLong(pong.millis)
  }

  def write(buf: ByteBuf)(id: Int)(packet: HandshakeClientboundPacket): Unit = {
    writeVarInt32(id)(buf)
    id match {
      case ResponsePacket.id =>
        writeResponse(buf)(packet.asInstanceOf[ResponsePacket])
      case PongPacket.id =>
        writePong(buf)(packet.asInstanceOf[PongPacket])
      case _ => // Nothing
    }
  }
}
