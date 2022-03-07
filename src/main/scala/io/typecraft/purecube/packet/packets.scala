package io.typecraft.purecube.packet

sealed trait Packet

case class HandshakePacket(
    protocolVersion: Int,
    serverAddress: String,
    serverPort: Int,
    nextState: Int
) extends Packet

case class PingPacket(
    millis: Long
) extends Packet

case class PongPacket(
    millis: Long
) extends Packet

case class ResponsePacket(
    json: String
) extends Packet
