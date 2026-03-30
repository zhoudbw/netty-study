package com.kernelcode.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 游戏聊天服务器
 */
public class ChatServer {
    private static final int SERVER_PORT = 8080;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelManager channelManager;
    
    public static void main( String[] args ) {
        ChatServer server = new ChatServer();
        server.start();
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        bossGroup = new NioEventLoopGroup( 1 );
        workerGroup = new NioEventLoopGroup();
        
        channelManager = new ChannelManager();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group( bossGroup, workerGroup ).channel( NioServerSocketChannel.class ).option( ChannelOption.SO_BACKLOG, 128 ).childOption( ChannelOption.SO_KEEPALIVE, true ).childHandler( new ChannelInitializer< SocketChannel >() {
                @Override
                protected void initChannel( SocketChannel ch ) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast( "encoder", new PacketEncoder() );
                    pipeline.addLast( "decoder", new PacketDecoder() );
                    pipeline.addLast( "handler", new ServerHandler( ChatServer.this, channelManager ) );
                }
            } );
            
            ChannelFuture future = bootstrap.bind( SERVER_PORT ).sync();
            
            System.out.println( "========================================" );
            System.out.println( "   游戏聊天服务器已启动" );
            System.out.println( "   监听端口: " + SERVER_PORT );
            System.out.println( "========================================" );
            
            future.channel().closeFuture().sync();
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    /**
     * 关闭服务器
     */
    public void shutdown() {
        if( bossGroup != null ) {
            bossGroup.shutdownGracefully();
        }
        if( workerGroup != null ) {
            workerGroup.shutdownGracefully();
        }
    }
}
