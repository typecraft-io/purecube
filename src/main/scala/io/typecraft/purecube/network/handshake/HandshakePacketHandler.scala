package io.typecraft.purecube.network.handshake

object HandshakePacketHandler {
  def handle(id: Int)(
      packet: HandshakeServerboundPacket
  ): Option[(Int, HandshakeClientboundPacket)] = {
    id match {
      case HandshakePacket.id =>
        val handshake = packet.asInstanceOf[HandshakePacket]
        Some(
          (
            ResponsePacket.id,
            ResponsePacket(
              s"""{
               |    "version": {
               |        "name": "1.8.7",
               |        "protocol": ${handshake.protocolVersion}
               |    },
               |    "players": {
               |        "max": 100,
               |        "online": 5,
               |        "sample": [
               |            {
               |                "name": "thinkofdeath",
               |                "id": "4566e69f-c907-48ee-8d71-d7ba5aa00d20"
               |            }
               |        ]
               |    },
               |    "description": {
               |        "text": "Hello world"
               |    },
               |    "favicon": "data:image/png;base64,<data>"
               |}""".stripMargin
            )
          )
        )
      case PingPacket.id =>
        val ping = packet.asInstanceOf[PingPacket]
        Some(
          (
            PongPacket.id,
            PongPacket(System.currentTimeMillis())
          )
        )
      case _ => None
    }
  }
}
