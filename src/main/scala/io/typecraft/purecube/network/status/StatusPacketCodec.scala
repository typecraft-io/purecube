package io.typecraft.purecube.network.status

import io.netty.buffer.ByteBuf
import io.typecraft.purecube.network.StringCodec.writeUTF
import io.typecraft.purecube.network.VarIntCodec.{readVarInt32, writeVarInt32}

object StatusPacketCodec {
  def readPing(buf: ByteBuf): PingPacket = PingPacket(
    buf.readLong()
  )

  def read(buf: ByteBuf): Option[(Int, StatusServerboundPacket)] = {
    val id = readVarInt32(buf)
    id match {
      case RequestPacket.id =>
        Some((id, RequestPacket))
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

  def write(buf: ByteBuf)(id: Int)(packet: StatusClientboundPacket): Unit = {
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
