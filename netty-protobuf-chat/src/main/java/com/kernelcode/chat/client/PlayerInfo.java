package com.kernelcode.chat.client;

/**
 * 玩家信息
 */
public class PlayerInfo {
    private String playerId;
    private String playerName;
    private int level;
    
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
}
