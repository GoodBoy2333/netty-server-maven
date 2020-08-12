/*
package com.fy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.util.List;

*/
/**
 * <p>
 *  MessagePack解码器
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 16:07
 *//*

public class MsgPackDecoder extends MessageToMessageDecoder<Object> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) throws Exception {
        */
/*ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(bytes);
        String s = messageUnpacker.unpackString();
        list.add(s);*//*

        ByteBuf byteBuf = (ByteBuf) o;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        String name = unpacker.unpackString();
        int age = unpacker.unpackInt();
        String address = unpacker.unpackString();
        UserInfo userInfo = new UserInfo(name, age, address);
        list.add(userInfo);
    }
}
*/
