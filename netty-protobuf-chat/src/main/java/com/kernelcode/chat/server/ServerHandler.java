package com.kernelcode.chat.server;

import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端处理器
 */
public class ServerHandler extends SimpleChannelInboundHandler< ChatProtocol.GameMessage > {
    
    private ChatServer server;
    private ChannelManager channelManager;
    
    public ServerHandler( ChatServer server, ChannelManager channelManager ) {
        this.server = server;
        this.channelManager = channelManager;
    }
    
    @Override
    protected void channelRead0( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) throws Exception {
        ChatProtocol.MsgType msgType = msg.getMsgType();
        
        switch( msgType ) {
            case CONNECT_REQ:
                handleConnectReq( ctx, msg );
                break;
            case HEARTBEAT:
                handleHeartbeat( ctx, msg );
                break;
            case CHAT_REQ:
                handleChatReq( ctx, msg );
                break;
            case JOIN_CHANNEL_REQ:
                handleJoinChannelReq( ctx, msg );
                break;
            case LEAVE_CHANNEL_REQ:
                handleLeaveChannelReq( ctx, msg );
                break;
            default:
                System.out.println( "收到未知消息类型: " + msgType );
        }
    }
    
    /**
     * 处理连接请求
     */
    private void handleConnectReq( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) {
        ChatProtocol.ConnectReq req = ChatProtocol.ConnectReq.parseFrom( msg.getData() );
        
        // 创建玩家信息
        PlayerInfo playerInfo = new PlayerInfo( req.getPlayerId(), req.getPlayerName() );
        playerInfo.setLevel( req.getLevel() );
        
        // 添加玩家连接
        channelManager.addPlayer( req.getPlayerId(), playerInfo, ctx.channel() );
        
        // 响应连接
        ChatProtocol.ConnectResp resp = ChatProtocol.ConnectResp.newBuilder().setCode( 0 ).setMessage( "连接成功" ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.CONNECT_RESP, resp );
        
        System.out.println( "玩家连接: " + req.getPlayerId() + ", " + req.getPlayerName() );
    }
    
    /**
     * 处理心跳
     */
    private void handleHeartbeat( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) {
        ChatProtocol.HeartBeat heartBeat = ChatProtocol.HeartBeat.parseFrom( msg.getData() );
        
        // 响应心跳
        ChatProtocol.HeartBeat heartbeatAck = ChatProtocol.HeartBeat.newBuilder().setTimestamp( heartBeat.getTimestamp() ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.HEARTBEAT_ACK, heartbeatAck );
    }
    
    /**
     * 处理聊天消息请求
     */
    private void handleChatReq( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) {
        ChatProtocol.ChatReq req = ChatProtocol.ChatReq.parseFrom( msg.getData() );
        PlayerSession session = channelManager.getPlayerSession( ctx.channel() );
        
        if( session == null ) {
            sendErrorMsg( ctx, "未连接" );
            return;
        }
        
        // 生成消息ID
        String messageId = generateMessageId();
        
        // 构建聊天消息
        ChatProtocol.PlayerInfo senderInfo = buildPlayerInfo( session.getPlayerInfo() );
        
        ChatProtocol.ChatMessage chatMessage = ChatProtocol.ChatMessage.newBuilder().setMessageId( messageId ).setSenderId( session.getPlayerId() ).setSenderName( session.getPlayerInfo().getPlayerName() ).setReceiverId( req.getReceiverId() ).setContent( req.getContent() ).setChannelType( req.getChannelType() ).setTimestamp( System.currentTimeMillis() ).setChannelName( req.getChannelName() ).setSenderInfo( senderInfo ).build();
        
        // 根据频道类型发送消息
        if( req.getChannelType() == ChatProtocol.ChannelType.PRIVATE ) {
            // 私聊：发送给接收者
            sendPrivateMessage( req.getReceiverId(), chatMessage );
            
            // 同时发送给发送者
            sendChatNotify( ctx.channel(), chatMessage );
        } else {
            // 频道消息：广播到频道
            broadcastToChannel( req.getChannelType(), req.getChannelName(), chatMessage );
        }
        
        // 响应发送成功
        ChatProtocol.ChatResp resp = ChatProtocol.ChatResp.newBuilder().setCode( 0 ).setMessage( "发送成功" ).setMessageId( messageId ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.CHAT_RESP, resp );
        
        System.out.println( "玩家 " + session.getPlayerId() + " 发送消息: " + req.getContent() );
    }
    
    /**
     * 处理加入频道请求
     */
    private void handleJoinChannelReq( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) {
        ChatProtocol.JoinChannelReq req = ChatProtocol.JoinChannelReq.parseFrom( msg.getData() );
        PlayerSession session = channelManager.getPlayerSession( ctx.channel() );
        
        if( session == null ) {
            sendErrorMsg( ctx, "未连接" );
            return;
        }
        
        // 加入频道
        channelManager.joinChannel( session.getPlayerId(), req.getChannelType(), req.getChannelName() );
        
        // 获取频道内玩家信息
        List< PlayerInfo > players = channelManager.getChannelPlayerInfos( req.getChannelType(), req.getChannelName() );
        
        // 构建玩家信息列表
        List< ChatProtocol.PlayerInfo > playerInfos = new ArrayList<>();
        for( PlayerInfo player : players ) {
            playerInfos.add( buildPlayerInfo( player ) );
        }
        
        // 响应加入频道
        ChatProtocol.JoinChannelResp resp = ChatProtocol.JoinChannelResp.newBuilder().setCode( 0 ).setMessage( "加入频道成功" ).addAllPlayers( playerInfos ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.JOIN_CHANNEL_RESP, resp );
    }
    
    /**
     * 处理离开频道请求
     */
    private void handleLeaveChannelReq( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) {
        ChatProtocol.LeaveChannelReq req = ChatProtocol.LeaveChannelReq.parseFrom( msg.getData() );
        PlayerSession session = channelManager.getPlayerSession( ctx.channel() );
        
        if( session == null ) {
            sendErrorMsg( ctx, "未连接" );
            return;
        }
        
        // 离开频道
        channelManager.leaveChannel( session.getPlayerId(), req.getChannelType(), req.getChannelName() );
        
        // 响应离开频道
        ChatProtocol.LeaveChannelResp resp = ChatProtocol.LeaveChannelResp.newBuilder().setCode( 0 ).setMessage( "离开频道成功" ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.LEAVE_CHANNEL_RESP, resp );
    }
    
    /**
     * 发送私聊消息
     */
    private void sendPrivateMessage( String receiverId, ChatProtocol.ChatMessage chatMessage ) {
        Channel receiverChannel = channelManager.getPlayerChannel( receiverId );
        if( receiverChannel != null && receiverChannel.isActive() ) {
            sendChatNotify( receiverChannel, chatMessage );
        }
    }
    
    /**
     * 广播消息到频道
     */
    private void broadcastToChannel( ChatProtocol.ChannelType channelType, String channelName, ChatProtocol.ChatMessage chatMessage ) {
        ChatProtocol.ChatNotify notify = ChatProtocol.ChatNotify.newBuilder().setChatMessage( chatMessage ).build();
        
        ChatProtocol.GameMessage gameMsg = ChatProtocol.GameMessage.newBuilder().setMsgType( ChatProtocol.MsgType.CHAT_NOTIFY ).setData( notify.toByteArray() ).build();
        
        channelManager.broadcastToChannel( channelType, channelName, gameMsg );
    }
    
    /**
     * 发送聊天消息通知
     */
    private void sendChatNotify( Channel channel, ChatProtocol.ChatMessage chatMessage ) {
        ChatProtocol.ChatNotify notify = ChatProtocol.ChatNotify.newBuilder().setChatMessage( chatMessage ).build();
        
        ChatProtocol.GameMessage gameMsg = ChatProtocol.GameMessage.newBuilder().setMsgType( ChatProtocol.MsgType.CHAT_NOTIFY ).setData( notify.toByteArray() ).build();
        
        channel.writeAndFlush( gameMsg );
    }
    
    /**
     * 发送消息
     */
    private void sendMsg( ChannelHandlerContext ctx, ChatProtocol.MsgType msgType, com.google.protobuf.GeneratedMessageV3 message ) {
        ChatProtocol.GameMessage gameMsg = ChatProtocol.GameMessage.newBuilder().setMsgType( msgType ).setData( message.toByteArray() ).build();
        
        ctx.writeAndFlush( gameMsg );
    }
    
    /**
     * 发送错误消息
     */
    private void sendErrorMsg( ChannelHandlerContext ctx, String errorMsg ) {
        ChatProtocol.ConnectResp resp = ChatProtocol.ConnectResp.newBuilder().setCode( -1 ).setMessage( errorMsg ).build();
        
        sendMsg( ctx, ChatProtocol.MsgType.CONNECT_RESP, resp );
    }
    
    /**
     * 构建玩家信息
     */
    private ChatProtocol.PlayerInfo buildPlayerInfo( PlayerInfo playerInfo ) {
        return ChatProtocol.PlayerInfo.newBuilder().setPlayerId( playerInfo.getPlayerId() ).setPlayerName( playerInfo.getPlayerName() ).setLevel( playerInfo.getLevel() ).setAvatarUrl( playerInfo.getAvatarUrl() ).setSignature( playerInfo.getSignature() ).setIsVip( playerInfo.isVip() ).build();
    }
    
    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception {
        System.out.println( "客户端连接: " + ctx.channel().remoteAddress() );
        super.channelActive( ctx );
    }
    
    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
        System.out.println( "客户端断开: " + ctx.channel().remoteAddress() );
        
        // 移除玩家
        channelManager.removePlayer( ctx.channel() );
        
        super.channelInactive( ctx );
    }
    
    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        System.err.println( "发生异常: " + cause.getMessage() );
        cause.printStackTrace();
        ctx.close();
    }
}

