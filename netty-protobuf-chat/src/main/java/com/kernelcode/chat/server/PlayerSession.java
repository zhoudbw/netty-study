package com.kernelcode.chat.server;

/**
 * 玩家会话
 */
public class PlayerSession {
    private String playerId;
    private PlayerInfo playerInfo;
    private io.netty.channel.Channel channel;
    
    public PlayerSession( String playerId, PlayerInfo playerInfo, io.netty.channel.Channel channel ) {
        this.playerId = playerId;
        this.playerInfo = playerInfo;
        this.channel = channel;
    }
    
    // Getter和Setter
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId( String playerId ) {
        this.playerId = playerId;
    }
    
    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
    
    public void setPlayerInfo( PlayerInfo playerInfo ) {
        this.playerInfo = playerInfo;
    }
    
    public io.netty.channel.Channel getChannel() {
        return channel;
    }
    
    public void setChannel( io.netty.channel.Channel channel ) {
        this.channel = channel;
    }
}
