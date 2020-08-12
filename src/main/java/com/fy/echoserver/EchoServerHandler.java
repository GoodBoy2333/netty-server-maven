package com.fy.echoserver;

import com.fy.protobuf.UserInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.net.SocketAddress;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 13:39
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*String message = (String) msg;
        System.out.println("Receive:[" + message + "],count:[" + (++count) + "]");
        message += "$";
        ByteBuf buf = Unpooled.copiedBuffer(message.getBytes());
        ctx.writeAndFlush(buf);*/
        /*String message = (String) msg;
        System.out.println("Receive:[" + message + "],count:[" + (++count) + "]");*/
        try {
            UserInfo.UserMsg userMsg = (UserInfo.UserMsg) msg;
            System.out.println("Receive:[" + userMsg.toString() + "],count:[" + (++count) + "]");
            UserInfo.UserMsg.Builder builder = userMsg.toBuilder().setState(1);
            ctx.writeAndFlush(builder);
        } finally {
            ReferenceCountUtil.release(msg);
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
