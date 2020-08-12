package com.fy.CustomServer;

import com.fy.protobuf.CustomMessageData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p >
 *
 * @author fangyan
 * @since 2020/8/11 18:09
 */
public class CustomServerHeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomMessageData.MessageData messageData = (CustomMessageData.MessageData) msg;
        if (messageData.getOrder() == CustomMessageData.MessageData.DataType.PING) {
            CustomMessageData.MessageData req = CustomMessageData.MessageData.newBuilder()
                    .setOrder(CustomMessageData.MessageData.DataType.PONG).build();
            System.out.println("Send-Client:PONG,time:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ctx.writeAndFlush(req);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
