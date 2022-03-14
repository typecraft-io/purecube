package io.typecraft.purecube.network.handshake

import io.netty.buffer.ByteBuf

object HandshakePacketHandler {
  def handle(buf: ByteBuf): Option[HandshakePacket] = {
    for {
      (_, packet) <- HandshakePacketCodec.read(buf)
      handshake <- packet match {
        case handshake: HandshakePacket =>
          Some(handshake)
        case _ => None
      }
    } yield handshake
  }
}
