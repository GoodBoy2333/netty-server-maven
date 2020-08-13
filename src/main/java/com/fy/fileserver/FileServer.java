package com.fy.fileserver;

import com.fy.echoserver.EchoServerHandler;
import com.fy.protobuf.UserInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 13:27
 */
public class FileServer {
    // 文件访问根路径
    private final String URL="/src/main/java/com/fy/protobuf/";

    public void bind(int port) {
        // 处理连接请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 处理IO事件
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    // 指定socket类型
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 向IO处理事件组中添加处理器，实际上这就像是一条处理消息的流水线
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            /*
                            首先向ChannelPipeline中添加HTTP请求消息解码器，随后又添加了HttpObjectAggregator解码器，
                            它的作用是将多个消息转换为单一的FulHttpRequest或者FullHttpResponse，原因是HTTP解码器在每
                            个HTTP消息中会生成多个消息对象。
                            (1) HttpRequest / HttpResponse;
                            (2) HttpContent;
                            (3) LastHttpContent.
                            */
                            socketChannel.pipeline()
                                    .addLast(new HttpRequestDecoder())
                                    .addLast(new HttpObjectAggregator(65536))
                                    //新增HTTP响应编码器，对HTTP响应消息进行编码;
                                    .addLast(new HttpResponseEncoder())
                                    //新增Chunked handler它的主要作用是支持异步发送大的码流(例如大的文件传输)，但不占用过多的内存,防止发生Java内存溢出错误
                                    .addLast(new ChunkedWriteHandler())
                                    //逻辑处理
                                    .addLast(new HttpFileServerHandler(URL));
                        }
                    });
            // 绑定端口同步等待启动成功
            ChannelFuture sync = bootstrap.bind(8080).sync();

            // 等待服务监听端口关闭
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
