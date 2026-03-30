package com.kernelcode.chat.server;

import com.kernelcode.chat.protocol.ChatProtocol;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 频道管理器
 */
public class ChannelManager {
    
    // 玩家ID与Channel的映射
    private ConcurrentHashMap< String, Channel > playerChannels = new ConcurrentHashMap<>();
    
    // Channel与玩家信息的映射
    private ConcurrentHashMap< Channel, PlayerSession > channelSessions = new ConcurrentHashMap<>();
    
    // 频道管理
    private ConcurrentHashMap< String, ChannelGroup > channelGroups = new ConcurrentHashMap<>();
    
    /**
     * 添加玩家连接
     */
    public void addPlayer( String playerId, PlayerInfo playerInfo, Channel channel ) {
        playerChannels.put( playerId, channel );
        
        PlayerSession session = new PlayerSession( playerId, playerInfo, channel );
        channelSessions.put( channel, session );
        
        System.out.println( "玩家连接: " + playerId + ", " + playerInfo.getPlayerName() );
    }
    
    /**
     * 移除玩家连接
     */
    public void removePlayer( Channel channel ) {
        PlayerSession session = channelSessions.remove( channel );
        if( session != null ) {
            // 从所有频道中移除
            leaveAllChannels( session.getPlayerId() );
            
            // 从映射中移除
            playerChannels.remove( session.getPlayerId() );
            
            System.out.println( "玩家断开: " + session.getPlayerId() );
        }
    }
    
    /**
     * 获取玩家Channel
     */
    public Channel getPlayerChannel( String playerId ) {
        return playerChannels.get( playerId );
    }
    
    /**
     * 获取玩家会话
     */
    public PlayerSession getPlayerSession( Channel channel ) {
        return channelSessions.get( channel );
    }
    
    /**
     * 加入频道
     */
    public void joinChannel( String playerId, ChatProtocol.ChannelType channelType, String channelName ) {
        String channelKey = getChannelKey( channelType, channelName );
        
        channelGroups.computeIfAbsent( channelKey, k -> new ChannelGroup() ).addPlayer( playerId );
        
        System.out.println( "玩家 " + playerId + " 加入频道: " + channelKey );
    }
    
    /**
     * 离开频道
     */
    public void leaveChannel( String playerId, ChatProtocol.ChannelType channelType, String channelName ) {
        String channelKey = getChannelKey( channelType, channelName );
        
        ChannelGroup group = channelGroups.get( channelKey );
        if( group != null ) {
            group.removePlayer( playerId );
            
            // 如果频道为空，移除频道
            if( group.getPlayerCount() == 0 ) {
                channelGroups.remove( channelKey );
            }
        }
        
        System.out.println( "玩家 " + playerId + " 离开频道: " + channelKey );
    }
    
    /**
     * 离开所有频道
     */
    public void leaveAllChannels( String playerId ) {
        for( ChannelGroup group : channelGroups.values() ) {
            group.removePlayer( playerId );
        }
    }
    
    /**
     * 获取频道中的玩家
     */
    public List< String > getChannelPlayers( ChatProtocol.ChannelType channelType, String channelName ) {
        String channelKey = getChannelKey( channelType, channelName );
        ChannelGroup group = channelGroups.get( channelKey );
        
        if( group != null ) {
            return new ArrayList<>( group.getPlayers() );
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 获取频道所有玩家信息
     */
    public List< PlayerInfo > getChannelPlayerInfos( ChatProtocol.ChannelType channelType, String channelName ) {
        List< String > playerIds = getChannelPlayers( channelType, channelName );
        List< PlayerInfo > playerInfos = new ArrayList<>();
        
        for( String playerId : playerIds ) {
            PlayerSession session = channelSessions.get( playerChannels.get( playerId ) );
            if( session != null ) {
                playerInfos.add( session.getPlayerInfo() );
            }
        }
        
        return playerInfos;
    }
    
    /**
     * 广播消息到频道
     */
    public void broadcastToChannel( ChatProtocol.ChannelType channelType, String channelName, ChatProtocol.GameMessage message ) {
        String channelKey = getChannelKey( channelType, channelName );
        ChannelGroup group = channelGroups.get( channelKey );
        
        if( group != null ) {
            for( String playerId : group.getPlayers() ) {
                Channel channel = playerChannels.get( playerId );
                if( channel != null && channel.isActive() ) {
                    channel.writeAndFlush( message );
                }
            }
        }
    }
    
    /**
     * 发送消息给指定玩家
     */
    public void sendToPlayer( String playerId, ChatProtocol.GameMessage message ) {
        Channel channel = playerChannels.get( playerId );
        if( channel != null && channel.isActive() ) {
            channel.writeAndFlush( message );
        }
    }
    
    /**
     * 获取在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return playerChannels.size();
    }
    
    /**
     * 获取频道Key
     */
    private String getChannelKey( ChatProtocol.ChannelType channelType, String channelName ) {
        if( channelName == null || channelName.isEmpty() ) {
            return channelType.name();
        }
        return channelType.name() + ":" + channelName;
    }
}