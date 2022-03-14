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
import io.netty.handler.codec.protobuf.{
  ProtobufVarint32FrameDecoder,
  ProtobufVarint32LengthFieldPrepender
}
import io.netty.util.AttributeKey
import io.typecraft.purecube.network.PacketHandler

import java.net.InetSocketAddress

object Main {

  /*
  https://wiki.vg/Protocol#Login
   */
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
            .addLast(new ProtobufVarint32FrameDecoder())
            .addLast(new ProtobufVarint32LengthFieldPrepender())
            .addLast(new SimpleChannelInboundHandler[ByteBuf] {
              override def channelRead0(
                  ctx: ChannelHandlerContext,
                  buf: ByteBuf
              ): Unit = {
                PacketHandler.handle(buf)(ctx)
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
