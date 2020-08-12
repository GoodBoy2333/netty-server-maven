package com.fy.CustomServer;

import com.fy.protobuf.CustomMessageData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * 自定义服务消息处理
 * 消息类型：
 * message MessageData{
 * int64 length = 1;
 * Content content = 2;
 * enum DataType {
 * REQ_LOGIN = 0;  //上线登录验证环节 等基础信息上报
 * RSP_LOGIN = 1;  //返回上线登录状态与基础信息
 * PING = 2;  //心跳
 * PONG = 3;  //心跳
 * REQ_ACT = 4;  //动作请求
 * RSP_ACT = 5;  //动作响应
 * REQ_CMD = 6;  //指令请求
 * RSP_CMD = 7;  //指令响应
 * REQ_LOG = 8 ;//日志请求
 * RSP_LOG = 9;  //日志响应
 * }
 * DataType order = 3;
 * message Content{
 * int64 contentLength = 1;
 * string data = 2;
 * }
 * }
 * </p >
 *
 * @author fangyan
 * @since 2020/8/11 16:17
 */
public class CustomServerHandler extends ChannelInboundHandlerAdapter {

    private String[] whiteIPv4List = {"127.0.0.1", "192.168.1.188"};
    public static ConcurrentHashMap nodeCheck = new ConcurrentHashMap();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomMessageData.MessageData messageData = (CustomMessageData.MessageData) msg;
        if (messageData.getOrder() == CustomMessageData.MessageData.DataType.UNRECOGNIZED) {
            // 无法识别的消息类型
            ctx.close();
        }

        if (messageData.getOrder() == CustomMessageData.MessageData.DataType.REQ_LOGIN) {
            // 检查白名单
            String nodeIndex = ctx.channel().remoteAddress().toString();
            if (nodeCheck.contains(nodeIndex)) {
                // 重复登录
                ctx.writeAndFlush(builderResp(false));
                return;
            } else {
                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = socketAddress.getAddress().getHostAddress();
                boolean isOk = false;
                for (String s : whiteIPv4List) {
                    if (s.equals(ip)) {
                        isOk = true;
                        break;
                    }
                }
                CustomMessageData.MessageData responseData = isOk ? builderResp(true) : builderResp(false);
                if (isOk) {
                    nodeCheck.put(nodeIndex, true);
                }
                ctx.writeAndFlush(responseData);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

    public CustomMessageData.MessageData builderResp(boolean isOk) {
        String r = isOk ? "SUCCESS" : "FAILED";
        CustomMessageData.MessageData.Content responseContent = CustomMessageData.MessageData.Content.newBuilder().setData(r).setContentLength(r.length()).build();
        CustomMessageData.MessageData responseData = CustomMessageData.MessageData.newBuilder().setOrder(CustomMessageData.MessageData.DataType.RSP_LOGIN).setContent(responseContent).build();
        return responseData;
    }
}
