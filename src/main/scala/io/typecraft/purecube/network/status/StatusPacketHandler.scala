package io.typecraft.purecube.network.status

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

object StatusPacketHandler {
  def handle(
      protocolVersion: Int
  )(buf: ByteBuf)(ctx: ChannelHandlerContext): Unit = {
    for {
      (inId, inPacket) <- StatusPacketCodec.read(buf)
      _ = println(s"--> ${inPacket}")
      (outId, outPacket) <- getResponse(protocolVersion)(inId)(inPacket)
      _ = {
        val outBuf = ctx.alloc().ioBuffer()
        StatusPacketCodec.write(outBuf)(outId)(outPacket)
        ctx.writeAndFlush(outBuf)
        println(s"<-- ${outPacket}")
      }
    } yield ()
  }

  def getResponse(protocolVersion: Int)(id: Int)(
      packet: StatusServerboundPacket
  ): Option[(Int, StatusClientboundPacket)] = {
    id match {
      case RequestPacket.id =>
        Some(
          (
            ResponsePacket.id,
            ResponsePacket(
              s"""{
                 |    "version": {
                 |        "name": "1.8.7",
                 |        "protocol": ${protocolVersion}
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
