package com.kernelcode.chat.server;

import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 客户端解码器
 */
public class PacketDecoder extends ByteToMessageDecoder {
    
    @Override
    protected void decode( ChannelHandlerContext ctx, ByteBuf in, List< Object > out ) throws Exception {
        if( in.readableBytes() < 4 ) {
            return;
        }
        
        in.markReaderIndex();
        
        int dataLength = in.readInt();
        
        if( in.readableBytes() < dataLength ) {
            in.resetReaderIndex();
            return;
        }
        
        byte[] data = new byte[ dataLength ];
        in.readBytes( data );
        
        ChatProtocol.GameMessage gameMsg = ChatProtocol.GameMessage.parseFrom( data );
        
        out.add( gameMsg );
    }
}
