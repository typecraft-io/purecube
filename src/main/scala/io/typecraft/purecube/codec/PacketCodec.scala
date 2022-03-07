package io.typecraft.purecube.codec

import io.netty.buffer.ByteBuf
import io.typecraft.purecube.codec.StringCodec.{readUTF, writeUTF}
import io.typecraft.purecube.codec.VarIntCodec.{readVarInt32, writeVarInt32}
import io.typecraft.purecube.packet._

// TODO: NettyPacketCodec[Packet]
object PacketCodec {
  def readPacket(buf: ByteBuf): Option[Packet] = {
    val id = readVarInt32(buf)
    id match {
      case 0x00 =>
        Some(readHandshake(buf))
      case 0x01 =>
        Some(readPing(buf))
      case _ =>
        None
    }
  }

  def writePacket(id: Int)(f: ByteBuf => Unit)(buf: ByteBuf): Unit = {
    writeVarInt32(id)(buf)
    f(buf)
  }

  def writePacket(packet: Packet)(buf: ByteBuf): Unit = {
    packet match {
      case PongPacket(millis)   => writePong(PongPacket(millis))(buf)
      case ResponsePacket(json) => writeResponse(ResponsePacket(json))(buf)
      case _                    => // Nothing
    }
  }

  def readHandshake(buf: ByteBuf): HandshakePacket = HandshakePacket(
    readVarInt32(buf),
    readUTF(buf),
    buf.readUnsignedShort(),
    readVarInt32(buf)
  )

  def readPing(buf: ByteBuf): PingPacket = PingPacket(
    buf.readLong()
  )

  def writePong(packet: PongPacket)(buf: ByteBuf): Unit = {
    buf.writeLong(packet.millis)
  }

  def writeResponse(packet: ResponsePacket)(buf: ByteBuf): Unit = {
    writeUTF(packet.json)(buf)
  }
}
