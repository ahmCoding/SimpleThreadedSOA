package org.example.cache;

/**
 * Representiert ein Element im Cache.
 */
public class CacheItem {
    private String command;
    private String result;
    private long createdAt;
    private long lastAccessedAt;

    public CacheItem(String command, String result) {
        this.command = command;
        this.result = result;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = System.currentTimeMillis();
    }

    public String getCommand() {
        return command;
    }

    public String getResult() {
        return result;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
}
