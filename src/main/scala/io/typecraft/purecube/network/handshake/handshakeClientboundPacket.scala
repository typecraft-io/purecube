package io.typecraft.purecube.network.handshake

sealed trait HandshakeClientboundPacket

case class ResponsePacket(
    json: String
) extends HandshakeClientboundPacket

object ResponsePacket {
  val id: Int = 0x00
}

case class PongPacket(
    millis: Long
) extends HandshakeClientboundPacket

object PongPacket {
  val id: Int = 0x01
}
