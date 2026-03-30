package com.kernelcode.chat.server;

import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 客户端编码器
 */
public class PacketEncoder extends MessageToByteEncoder< ChatProtocol.GameMessage > {
    
    @Override
    protected void encode( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg, ByteBuf out ) throws Exception {
        byte[] data = msg.toByteArray();
        
        // 写入数据长度（4字节）
        out.writeInt( data.length );
        
        // 写入数据
        out.writeBytes( data );
    }
}