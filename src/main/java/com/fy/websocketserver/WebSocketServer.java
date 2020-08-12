package com.fy.websocketserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/10 15:30
 */
public class WebSocketServer {
    public void bind(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //将请求和应答消息编码或者解码为HTTP消息
                                    .addLast(new HttpServerCodec())
                                    //将HTTP消息的多个部分组合成一条完整的HTTP消息,Aggregator:聚合，组合
                                    .addLast(new HttpObjectAggregator(65536))
                                    //向客户端发送html5文件，它主要用于支持浏览器和服务端进行WebSocket通信
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new WebSocketServerHandler());
                        }
                    });
            Channel channel = bootstrap.bind(port).sync().channel();

            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
