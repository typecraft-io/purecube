package io.typecraft.purecube.network.status

sealed trait StatusClientboundPacket

case class ResponsePacket(json: String) extends StatusClientboundPacket

object ResponsePacket {
  val id: Int = 0x00
}

case class PongPacket(millis: Long) extends StatusClientboundPacket

object PongPacket {
  val id: Int = 0x01
}
