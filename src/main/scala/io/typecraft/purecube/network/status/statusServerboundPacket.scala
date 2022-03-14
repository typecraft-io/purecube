package io.typecraft.purecube.network.status

sealed trait StatusServerboundPacket

object RequestPacket extends StatusServerboundPacket {
  val id: Int = 0x00
}

case class PingPacket(millis: Long) extends StatusServerboundPacket

object PingPacket {
  val id: Int = 0x01
}
