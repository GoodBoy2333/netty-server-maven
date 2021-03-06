package com.fy.websocketserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * <p>
 * </p >
 *
 * @author fangyan
 * @since 2020/8/10 15:37
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    // 服务器端Web套接字打开和关闭握手基类

    WebSocketServerHandshaker handshaker;

    //webSocket默认端口：8080

    int port;

    public WebSocketServerHandler(int port) {
        this.port = port;
    }

    /**
     * channel 通道 action 活跃的 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
        Global.group.add(ctx.channel());
        System.out.println("客户端与服务端连接开启：" + ctx.channel().remoteAddress().toString());
    }

    /**
     * channel 通道 Inactive 不活跃的 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端关闭了通信通道并且不可以传输数据
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除
        Global.group.remove(ctx.channel());
        System.out.println("客户端与服务端连接关闭：" + ctx.channel().remoteAddress().toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (o instanceof FullHttpRequest) {
            // 处理http消息（升级协议）
            handlerHttpRequest(channelHandlerContext, (FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            // 处理websocket消息
            handlerWebSocketFrame(channelHandlerContext, (WebSocketFrame) o);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 如果不是升级协议消息
        if (!req.getDecoderResult().isSuccess() || !"websocket".equals(req.headers().get("Upgrade"))) {
            sendError(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        // 设置连接参数
        ctx.attr(AttributeKey.valueOf("channelId")).set(ctx.channel().id());
        System.out.println(ctx.channel().remoteAddress().toString() + "----" + ctx.channel().id());

        //websocket协议开头为：ws+ip+端口
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:" + this.port, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (wsFactory == null) {
            //返回不支持websocket 版本
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            //开始握手
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否为关闭链路指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
            return;
        }
        // ping请求返回pong
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 仅支持文本信息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame not support", frame.getClass().getName()));
        }
        String text = ((TextWebSocketFrame) frame).text();
        System.out.println(String.format("Client:%s,channelId:%s", text, ctx.attr(AttributeKey.valueOf("channelId")).get()));

        TextWebSocketFrame tws = new TextWebSocketFrame(String.format("服务器收到消息:%s,通道id:%s,当前时间:%s", text, ctx.channel().id(), LocalDateTime.now()));
        if (text.startsWith("$")) {
            //已$开头的单独回复
            ctx.channel().write(tws);
        } else {
            //群发
            Global.group.writeAndFlush(tws);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(byteBuf);
            byteBuf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.getStatus().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
