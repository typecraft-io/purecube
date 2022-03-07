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
import io.typecraft.purecube.codec.PacketCodec._
import io.typecraft.purecube.codec.VarIntCodec.{
  decodeFrame,
  encodeFrameAlloc,
  readVarInt32
}
import io.typecraft.purecube.packet._

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
                  (packet, response) <- decodeFrame(buf).map(onPacket)
                  _ = println(s"--> $packet")
                  (responsePacket, writer) <- response
                  _ = {
                    println(s"<-- $responsePacket")
                    ctx.writeAndFlush(encodeFrameAlloc(writer)(ctx.alloc()))
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

  // TODO: ugly return type?
  def onPacket(
      buf: ByteBuf
  ): (Option[Packet], Option[(Packet, ByteBuf => Unit)]) = {
    val id = readVarInt32(buf)
    id match {
      case 0x00 =>
        val handshake = readHandshake(buf)
        val response = ResponsePacket(
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
        (
          Some(handshake),
          Some(
            (
              response,
              writePacket(0x00)(writeResponse(response))
            )
          )
        )
      case 0x01 =>
        val ping = readPing(buf)
        val pong = PongPacket(System.currentTimeMillis())
        (
          Some(ping),
          Some(
            (
              pong,
              writePacket(0x01)(writePong(pong))
            )
          )
        )
      case _ => (None, None)
    }
  }
}
