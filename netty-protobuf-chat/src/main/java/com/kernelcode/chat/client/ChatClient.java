package com.kernelcode.chat.client;

import com.google.protobuf.GeneratedMessageV3;
import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 游戏聊天客户端
 */
public class ChatClient {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    
    private EventLoopGroup group;
    private Channel channel;
    private String playerId;
    private String playerName;
    private PlayerInfo playerInfo;
    
    // 当前频道
    private ChatProtocol.ChannelType currentChannel = ChatProtocol.ChannelType.WORLD;
    private String currentChannelName = "";
    
    // 在线玩家列表
    private ConcurrentHashMap< String, ChatProtocol.PlayerInfo > onlinePlayers = new ConcurrentHashMap<>();
    
    public static void main( String[] args ) {
        ChatClient client = new ChatClient();
        client.start();
    }
    
    /**
     * 启动客户端
     */
    public void start() {
        group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group( group ).channel( NioSocketChannel.class ).option( ChannelOption.TCP_NODELAY, true ).option( ChannelOption.SO_KEEPALIVE, true ).handler( new ChannelInitializer< SocketChannel >() {
                @Override
                protected void initChannel( SocketChannel ch ) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast( "encoder", new PacketEncoder() );
                    pipeline.addLast( "decoder", new PacketDecoder() );
                    pipeline.addLast( "handler", new ClientHandler( ChatClient.this ) );
                }
            } );
            
            // 连接服务器
            ChannelFuture future = bootstrap.connect( SERVER_HOST, SERVER_PORT ).sync();
            channel = future.channel();
            
            System.out.println( "========================================" );
            System.out.println( "   游戏聊天客户端已启动" );
            System.out.println( "   服务器: " + SERVER_HOST + ":" + SERVER_PORT );
            System.out.println( "========================================" );
            
            // 启动心跳
            startHeartbeat();
            
            // 等待连接建立
            Thread.sleep( 1000 );
            
            // 处理用户输入
            handleUserInput();
            
            // 等待连接关闭
            channel.closeFuture().sync();
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    /**
     * 处理用户输入
     */
    private void handleUserInput() {
        Scanner scanner = new Scanner( System.in );
        
        while( true ) {
            System.out.println( "\n========================================" );
            System.out.println( "当前频道: " + getChannelName( currentChannel ) );
            if( !currentChannelName.isEmpty() ) {
                System.out.println( "频道名称: " + currentChannelName );
            }
            System.out.println( "========================================" );
            System.out.println( "请选择操作:" );
            System.out.println( "1. 连接服务器" );
            System.out.println( "2. 发送消息" );
            System.out.println( "3. 切换频道" );
            System.out.println( "4. 查看在线玩家" );
            System.out.println( "5. 查看历史消息" );
            System.out.println( "6. 私聊" );
            System.out.println( "7. 退出" );
            System.out.println( "========================================" );
            System.out.print( "请输入选项: " );
            
            String choice = scanner.nextLine();
            
            switch( choice ) {
                case "1":
                    connect( scanner );
                    break;
                case "2":
                    sendMessage( scanner );
                    break;
                case "3":
                    switchChannel( scanner );
                    break;
                case "4":
                    showOnlinePlayers();
                    break;
                case "5":
                    getHistory( scanner );
                    break;
                case "6":
                    privateChat( scanner );
                    break;
                case "7":
                    System.out.println( "正在退出..." );
                    shutdown();
                    return;
                default:
                    System.out.println( "无效的选项!" );
            }
        }
    }
    
    /**
     * 连接服务器
     */
    private void connect( Scanner scanner ) {
        System.out.print( "请输入玩家ID: " );
        playerId = scanner.nextLine();
        
        System.out.print( "请输入玩家名称: " );
        playerName = scanner.nextLine();
        
        System.out.print( "请输入玩家等级: " );
        int level = Integer.parseInt( scanner.nextLine() );
        
        System.out.print( "请输入Token: " );
        String token = scanner.nextLine();
        
        // 构建连接请求
        ChatProtocol.ConnectReq connectReq = ChatProtocol.ConnectReq.newBuilder().setPlayerId( playerId ).setPlayerName( playerName ).setLevel( level ).setToken( token ).build();
        
        // 发送连接请求
        sendMsg( ChatProtocol.MsgType.CONNECT_REQ, connectReq );
        System.out.println( "正在连接服务器..." );
    }
    
    /**
     * 发送消息
     */
    private void sendMessage( Scanner scanner ) {
        if( playerId == null ) {
            System.out.println( "请先连接服务器!" );
            return;
        }
        
        System.out.print( "请输入消息内容: " );
        String content = scanner.nextLine();
        
        // 构建聊天请求
        ChatProtocol.ChatReq chatReq = ChatProtocol.ChatReq.newBuilder().setContent( content ).setChannelType( currentChannel ).setChannelName( currentChannelName ).build();
        
        // 发送聊天请求
        sendMsg( ChatProtocol.MsgType.CHAT_REQ, chatReq );
        System.out.println( "发送消息中..." );
    }
    
    /**
     * 切换频道
     */
    private void switchChannel( Scanner scanner ) {
        if( playerId == null ) {
            System.out.println( "请先连接服务器!" );
            return;
        }
        
        System.out.println( "请选择频道:" );
        System.out.println( "1. 世界频道" );
        System.out.println( "2. 公会频道" );
        System.out.println( "3. 队伍频道" );
        System.out.print( "请输入选项: " );
        
        String choice = scanner.nextLine();
        String channelName = "";
        
        switch( choice ) {
            case "1":
                currentChannel = ChatProtocol.ChannelType.WORLD;
                break;
            case "2":
                currentChannel = ChatProtocol.ChannelType.GUILD;
                System.out.print( "请输入公会名称: " );
                channelName = scanner.nextLine();
                currentChannelName = channelName;
                break;
            case "3":
                currentChannel = ChatProtocol.ChannelType.TEAM;
                System.out.print( "请输入队伍名称: " );
                channelName = scanner.nextLine();
                currentChannelName = channelName;
                break;
            default:
                System.out.println( "无效的选项!" );
                return;
        }
        
        // 离开当前频道
        leaveCurrentChannel();
        
        // 加入新频道
        joinChannel( currentChannel, channelName );
    }
    
    /**
     * 加入频道
     */
    public void joinChannel( ChatProtocol.ChannelType channelType, String channelName ) {
        ChatProtocol.JoinChannelReq req = ChatProtocol.JoinChannelReq.newBuilder().setChannelType( channelType ).setChannelName( channelName ).build();

        sendMsg( ChatProtocol.MsgType.JOIN_CHANNEL_REQ, req );
    }
    
    /**
     * 离开当前频道
     */
    private void leaveCurrentChannel() {
        ChatProtocol.LeaveChannelReq req = ChatProtocol.LeaveChannelReq.newBuilder().setChannelType( currentChannel ).setChannelName( currentChannelName ).build();
        
        sendMsg( ChatProtocol.MsgType.LEAVE_CHANNEL_REQ, req );
    }
    
    /**
     * 显示在线玩家
     */
    private void showOnlinePlayers() {
        if( playerId == null ) {
            System.out.println( "请先连接服务器!" );
            return;
        }
        
        System.out.println( "\n========================================" );
        System.out.println( "在线玩家列表:" );
        System.out.println( "玩家ID\t\t玩家名称\t等级" );
        System.out.println( String.join( "", Collections.nCopies( 50, "─" ) ) );
        
        for( ChatProtocol.PlayerInfo player : onlinePlayers.values() ) {
            System.out.printf( "%s\t%s\t%d\n", player.getPlayerId(), player.getPlayerName(), player.getLevel() );
        }
        
        System.out.println( "========================================" );
    }
    
    /**
     * 获取历史消息
     */
    private void getHistory( Scanner scanner ) {
        if( playerId == null ) {
            System.out.println( "请先连接服务器!" );
            return;
        }
        
        System.out.print( "请输入获取消息数量: " );
        int count = Integer.parseInt( scanner.nextLine() );
        
        // 构建历史消息请求
        ChatProtocol.HistoryReq req = ChatProtocol.HistoryReq.newBuilder().setChannelType( currentChannel ).setChannelName( currentChannelName ).setCount( count ).build();
        
        sendMsg( ChatProtocol.MsgType.HISTORY_REQ, req );
        System.out.println( "正在获取历史消息..." );
    }
    
    /**
     * 私聊
     */
    private void privateChat( Scanner scanner ) {
        if( playerId == null ) {
            System.out.println( "请先连接服务器!" );
            return;
        }
        
        System.out.print( "请输入对方玩家ID: " );
        String receiverId = scanner.nextLine();
        
        System.out.print( "请输入消息内容: " );
        String content = scanner.nextLine();
        
        // 构建私聊请求
        ChatProtocol.ChatReq chatReq = ChatProtocol.ChatReq.newBuilder().setReceiverId( receiverId ).setContent( content ).setChannelType( ChatProtocol.ChannelType.PRIVATE ).build();
        
        sendMsg( ChatProtocol.MsgType.CHAT_REQ, chatReq );
        System.out.println( "发送私聊消息中..." );
    }
    
    /**
     * 发送消息
     */
    public void sendMsg( ChatProtocol.MsgType msgType, GeneratedMessageV3 message ) {
        if( channel == null || !channel.isActive() ) {
            System.out.println( "连接未建立或已断开!" );
            return;
        }
        
        ChatProtocol.GameMessage gameMsg = ChatProtocol.GameMessage.newBuilder().setMsgType( msgType ).setData( com.google.protobuf.ByteString.copyFrom( message.toByteArray() ) ).build();
        
        channel.writeAndFlush( gameMsg );
    }
    
    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        channel.eventLoop().scheduleAtFixedRate(
                () -> {
                    if( channel != null && channel.isActive() ) {
                        ChatProtocol.HeartBeat heartBeat = ChatProtocol.HeartBeat.newBuilder().setTimestamp( System.currentTimeMillis() ).build();
                        
                        sendMsg( ChatProtocol.MsgType.HEARTBEAT, heartBeat );
                    }
                }, 10, 10, TimeUnit.SECONDS
        );
    }
    
    /**
     * 获取频道名称
     */
    private String getChannelName( ChatProtocol.ChannelType channelType ) {
        switch( channelType ) {
            case WORLD:
                return "世界频道";
            case PRIVATE:
                return "私聊";
            case GUILD:
                return "公会频道";
            case SYSTEM:
                return "系统消息";
            case TEAM:
                return "队伍频道";
            default:
                return "未知";
        }
    }
    
    /**
     * 关闭客户端
     */
    public void shutdown() {
        // 离开当前频道
        if( playerId != null ) {
            leaveCurrentChannel();
        }
        
        if( channel != null ) {
            channel.close();
        }
        if( group != null ) {
            group.shutdownGracefully();
        }
        System.exit( 0 );
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerInfo( PlayerInfo playerInfo ) {
        this.playerInfo = playerInfo;
    }
    
    public ConcurrentHashMap< String, ChatProtocol.PlayerInfo > getOnlinePlayers() {
        return onlinePlayers;
    }
}