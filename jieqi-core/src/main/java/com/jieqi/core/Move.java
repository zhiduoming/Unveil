package com.jieqi.core;

import com.jieqi.record.MoveNotation;

public class Move {
    private String source;
    private String destination;
    private Integer type;
    private long turnStartTime;
    private long clientTimestamp;
    private long serverTimestamp;
    private boolean isFlipOnly;
    /** 暗子揭开后的真实类型；-1 表示未揭开。用于复盘还原。 */
    private int revealedType = -1;

    public Move() {}

    public Move(String source, String destination) {
        this.source = source;
        this.destination = destination;
        this.turnStartTime = System.currentTimeMillis();
        this.serverTimestamp = this.turnStartTime;
        this.isFlipOnly = source.equals(destination);
    }

    public Move(String source, String destination, Integer type) {
        this(source, destination);
        this.type = type;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public long getTurnStartTime() { return turnStartTime; }
    public void setTurnStartTime(long turnStartTime) { this.turnStartTime = turnStartTime; }
    public long getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(long clientTimestamp) { this.clientTimestamp = clientTimestamp; }
    public long getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(long serverTimestamp) { this.serverTimestamp = serverTimestamp; }
    public boolean isFlipOnly() { return isFlipOnly; }
    public void setFlipOnly(boolean flipOnly) { isFlipOnly = flipOnly; }
    /** 暗子揭开后的真实类型；-1 表示未揭开 */
    public int getRevealedType() { return revealedType; }
    public void setRevealedType(int revealedType) { this.revealedType = revealedType; }
    /** 这一步是否揭开了暗子 */
    public boolean hasRevealed() { return revealedType >= 0; }

    public boolean isTimeout() {
        return System.currentTimeMillis() - turnStartTime > 65000;
    }

    @Override
    public String toString() {
        return MoveNotation.format(this);
    }
}