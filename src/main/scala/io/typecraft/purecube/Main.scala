package io.typecraft.purecube

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{
  ChannelHandlerContext,
  ChannelInitializer,
  SimpleChannelInboundHandler
}
import io.typecraft.purecube.network.VarIntCodec.{decodeFrame, encodeFrameAlloc}
import io.typecraft.purecube.network.handshake.{
  HandshakePacketCodec,
  HandshakePacketHandler
}

import java.net.InetSocketAddress

object Main {
  def main(args: Array[String]): Unit = {
    val group = new NioEventLoopGroup()
    try {
      val bootstrap = new ServerBootstrap
      bootstrap.group(group)
      bootstrap.channel(classOf[NioServerSocketChannel])
      bootstrap.localAddress(new InetSocketAddress("localhost", 25565))
      bootstrap.childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          ch.pipeline()
            .addLast(new SimpleChannelInboundHandler[ByteBuf] {
              override def channelRead0(
                  ctx: ChannelHandlerContext,
                  buf: ByteBuf
              ): Unit = {
                for {
                  (inPacketId, inPacket) <- decodeFrame(buf)
                    .flatMap(HandshakePacketCodec.read)
                  _ = println(s"--> $inPacket")
                  (outPacketId, outPacket) <- HandshakePacketHandler
                    .handle(inPacketId)(inPacket)
                  _ = {
                    println(s"<-- $outPacket")
                    ctx.writeAndFlush(
                      encodeFrameAlloc(
                        HandshakePacketCodec.write(_)(outPacketId)(outPacket)
                      )(ctx.alloc())
                    )
                  }
                } yield ()
              }
            })
        }
      })
      val future = bootstrap.bind.sync
      future.channel.closeFuture.sync
    } catch {
      case e: Exception =>
        e.printStackTrace()
    } finally {
      group.shutdownGracefully().sync
    }
  }
}
