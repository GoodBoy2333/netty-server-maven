/*
package com.fy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

*/
/**
 * <p>
 *  MsgPackEncoder编码器
 * </p >
 *
 * @author fangyan
 * @since 2020/8/9 16:02
 *//*

public class MsgPackEncoder extends MessageToByteEncoder<Object> {

    MessageBufferPacker mbp = MessagePack.newDefaultBufferPacker();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        */
/* MessageBufferPacker mbp = MessagePack.newDefaultBufferPacker();
        mbp.packString((String) o);
        byteBuf.writeBytes(mbp.toByteArray());*//*

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        UserInfo userInfo = (UserInfo) o;
        packer.packString(userInfo.getUserName());
        packer.packInt(userInfo.getAge());
        packer.packString(userInfo.getAddress());
        int length = packer.toByteArray().length;
        packer.packExtensionTypeHeader((byte) 0, length);
        packer.writePayload(packer.toByteArray());
        packer.close();
        byteBuf.writeBytes(packer.toByteArray());
    }
}
*/
