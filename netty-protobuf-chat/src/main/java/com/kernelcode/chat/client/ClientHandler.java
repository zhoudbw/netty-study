package com.kernelcode.chat.client;

import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端处理器
 */
public class ClientHandler extends SimpleChannelInboundHandler< ChatProtocol.GameMessage > {
    
    private ChatClient client;
    
    public ClientHandler( ChatClient client ) {
        this.client = client;
    }
    
    @Override
    protected void channelRead0( ChannelHandlerContext ctx, ChatProtocol.GameMessage msg ) throws Exception {
        ChatProtocol.MsgType msgType = msg.getMsgType();
        
        switch( msgType ) {
            case CONNECT_RESP:
                handleConnectResp( msg );
                break;
            case HEARTBEAT_ACK:
                handleHeartbeatAck( msg );
                break;
            case CHAT_RESP:
                handleChatResp( msg );
                break;
            case CHAT_NOTIFY:
                handleChatNotify( msg );
                break;
            case JOIN_CHANNEL_RESP:
                handleJoinChannelResp( msg );
                break;
            case LEAVE_CHANNEL_RESP:
                handleLeaveChannelResp( msg );
                break;
            case USER_LIST_NOTIFY:
                handleUserListNotify( msg );
                break;
            case HISTORY_RESP:
                handleHistoryResp( msg );
                break;
            default:
                System.out.println( "收到未知消息类型: " + msgType );
        }
    }
    
    /**
     * 处理连接响应
     */
    private void handleConnectResp( ChatProtocol.GameMessage msg ) {
        ChatProtocol.ConnectResp resp = ChatProtocol.ConnectResp.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "连接响应:" );
        System.out.println( "  响应码: " + resp.getCode() );
        System.out.println( "  消息: " + resp.getMessage() );
        
        if( resp.getCode() == 0 ) {
            System.out.println( "  连接成功!" );
            // 保存玩家信息
            client.setPlayerInfo( new PlayerInfo( client.getPlayerId(), client.getPlayerName() ) );
            
            // 自动加入世界频道
            client.joinChannel( ChatProtocol.ChannelType.WORLD, "" );
        }
        System.out.println( "========================================" );
    }
    
    /**
     * 处理心跳响应
     */
    private void handleHeartbeatAck( ChatProtocol.GameMessage msg ) {
        ChatProtocol.HeartBeat heartBeat = ChatProtocol.HeartBeat.parseFrom( msg.getData() );
        System.out.println( "收到心跳响应: " + heartBeat.getTimestamp() );
    }
    
    /**
     * 处理聊天消息响应
     */
    private void handleChatResp( ChatProtocol.GameMessage msg ) {
        ChatProtocol.ChatResp resp = ChatProtocol.ChatResp.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "发送消息响应:" );
        System.out.println( "  响应码: " + resp.getCode() );
        System.out.println( "  消息: " + resp.getMessage() );
        if( resp.getCode() == 0 ) {
            System.out.println( "  消息ID: " + resp.getMessageId() );
        }
        System.out.println( "========================================" );
    }
    
    /**
     * 处理聊天消息通知
     */
    private void handleChatNotify( ChatProtocol.GameMessage msg ) {
        ChatProtocol.ChatNotify notify = ChatProtocol.ChatNotify.parseFrom( msg.getData() );
        ChatProtocol.ChatMessage chatMessage = notify.getChatMessage();
        
        System.out.println( "\n========================================" );
        System.out.println( "收到新消息:" );
        System.out.println( "  频道: " + getChannelName( chatMessage.getChannelType() ) );
        if( !chatMessage.getChannelName().isEmpty() ) {
            System.out.println( "  频道名称: " + chatMessage.getChannelName() );
        }
        System.out.println( "  发送者: " + chatMessage.getSenderName() + " (" + chatMessage.getSenderId() + ")" );
        if( !chatMessage.getReceiverId().isEmpty() ) {
            System.out.println( "  接收者: " + chatMessage.getReceiverId() );
        }
        System.out.println( "  内容: " + chatMessage.getContent() );
        System.out.println( "  时间: " + new java.util.Date( chatMessage.getTimestamp() ) );
        System.out.println( "========================================" );
    }
    
    /**
     * 处理加入频道响应
     */
    private void handleJoinChannelResp( ChatProtocol.GameMessage msg ) {
        ChatProtocol.JoinChannelResp resp = ChatProtocol.JoinChannelResp.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "加入频道响应:" );
        System.out.println( "  响应码: " + resp.getCode() );
        System.out.println( "  消息: " + resp.getMessage() );
        
        if( resp.getCode() == 0 ) {
            System.out.println( "  频道内玩家:" );
            for( ChatProtocol.PlayerInfo player : resp.getPlayersList() ) {
                System.out.println( "    " + player.getPlayerName() + " (" + player.getPlayerId() + ")" );
                client.getOnlinePlayers().put( player.getPlayerId(), player );
            }
        }
        System.out.println( "========================================" );
    }
    
    /**
     * 处理离开频道响应
     */
    private void handleLeaveChannelResp( ChatProtocol.GameMessage msg ) {
        ChatProtocol.LeaveChannelResp resp = ChatProtocol.LeaveChannelResp.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "离开频道响应:" );
        System.out.println( "  响应码: " + resp.getCode() );
        System.out.println( "  消息: " + resp.getMessage() );
        System.out.println( "========================================" );
    }
    
    /**
     * 处理用户列表通知
     */
    private void handleUserListNotify( ChatProtocol.GameMessage msg ) {
        ChatProtocol.UserListNotify notify = ChatProtocol.UserListNotify.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "用户列表更新:" );
        System.out.println( "  频道: " + getChannelName( notify.getChannelType() ) );
        
        for( ChatProtocol.PlayerInfo player : notify.getPlayersList() ) {
            System.out.println( "    " + player.getPlayerName() + " (" + player.getPlayerId() + ")" );
            client.getOnlinePlayers().put( player.getPlayerId(), player );
        }
        System.out.println( "========================================" );
    }
    
    /**
     * 处理历史消息响应
     */
    private void handleHistoryResp( ChatProtocol.GameMessage msg ) {
        ChatProtocol.HistoryResp resp = ChatProtocol.HistoryResp.parseFrom( msg.getData() );
        
        System.out.println( "\n========================================" );
        System.out.println( "历史消息:" );
        System.out.println( "  响应码: " + resp.getCode() );
        System.out.println( "  消息: " + resp.getMessage() );
        
        if( resp.getCode() == 0 ) {
            for( ChatProtocol.ChatMessage chatMessage : resp.getMessagesList() ) {
                System.out.println( "  ──────────────────────────────────────" );
                System.out.println( "  发送者: " + chatMessage.getSenderName() );
                System.out.println( "  内容: " + chatMessage.getContent() );
                System.out.println( "  时间: " + new java.util.Date( chatMessage.getTimestamp() ) );
            }
        }
        System.out.println( "========================================" );
    }
    
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
    
    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception {
        System.out.println( "连接到服务器: " + ctx.channel().remoteAddress() );
        super.channelActive( ctx );
    }
    
    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
        System.out.println( "与服务器断开连接: " + ctx.channel().remoteAddress() );
        super.channelInactive( ctx );
    }
    
    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        System.err.println( "发生异常: " + cause.getMessage() );
        cause.printStackTrace();
        ctx.close();
    }
}