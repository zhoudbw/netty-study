package com.kernelcode.chat.server;

/**
 * 玩家信息
 */
public class PlayerInfo {
    private String playerId;
    private String playerName;
    private int level;
    private String avatarUrl;
    private String signature;
    private boolean isVip;
    
    public PlayerInfo( String playerId, String playerName ) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    // Getter和Setter
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId( String playerId ) {
        this.playerId = playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName( String playerName ) {
        this.playerName = playerName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel( int level ) {
        this.level = level;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl( String avatarUrl ) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature( String signature ) {
        this.signature = signature;
    }
    
    public boolean isVip() {
        return isVip;
    }
    
    public void setVip( boolean vip ) {
        isVip = vip;
    }
}