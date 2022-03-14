package io.typecraft.purecube.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import io.typecraft.purecube.network.handshake.{
  HandshakePacket,
  HandshakePacketCodec
}
import io.typecraft.purecube.network.status.StatusPacketHandler

object PacketHandler {
  val protocolTypeKey: AttributeKey[Int] = AttributeKey.valueOf("protocol")
  val protocolVersionKey: AttributeKey[Int] =
    AttributeKey.valueOf("protocolVersion")

  def handle(buf: ByteBuf)(ctx: ChannelHandlerContext): Unit = {
    val protocolTypeAttr = ctx.channel().attr(protocolTypeKey)
    val protocolVersionAttr = ctx.channel().attr(protocolVersionKey)
    protocolTypeAttr.get() match {
      case ProtocolType.handshake =>
        HandshakePacketCodec.read(buf).foreach {
          case (_, handshake: HandshakePacket) =>
            protocolTypeAttr.set(handshake.nextState)
            protocolVersionAttr.set(handshake.protocolVersion)
            println(s"--> ${handshake}")
          case _ => // Nothing
        }
      case ProtocolType.status =>
        StatusPacketHandler.handle(protocolVersionAttr.get())(buf)(ctx)
      case ProtocolType.login =>
        ???
      case _ =>
      // Nothing
    }
  }
}
