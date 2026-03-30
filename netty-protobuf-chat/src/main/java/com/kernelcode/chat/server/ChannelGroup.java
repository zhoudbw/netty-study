package com.kernelcode.chat.server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 频道组
 */
public class ChannelGroup {
    private ConcurrentHashMap< String, Boolean > players = new ConcurrentHashMap<>();
    
    /**
     * 添加玩家
     */
    public void addPlayer( String playerId ) {
        players.put( playerId, true );
    }
    
    /**
     * 移除玩家
     */
    public void removePlayer( String playerId ) {
        players.remove( playerId );
    }
    
    /**
     * 获取所有玩家
     */
    public ConcurrentHashMap< String, Boolean > getPlayers() {
        return players;
    }
    
    /**
     * 获取玩家数量
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * 检查玩家是否在频道中
     */
    public boolean containsPlayer( String playerId ) {
        return players.containsKey( playerId );
    }
}