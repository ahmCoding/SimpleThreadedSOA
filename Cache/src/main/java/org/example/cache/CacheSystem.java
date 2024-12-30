package org.example.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Representiert die Cache-Verwaltung.
 */
public class CacheSystem {
    private ConcurrentHashMap<String,CacheItem> cache ;
    private final Thread cleanCacheTask;

    public CacheSystem() {
        this.cache = new ConcurrentHashMap<>();
        this.cleanCacheTask = new Thread(new CleanCacheTask(this));
        this.cleanCacheTask.start();
    }
    /**
     * Fügt ein Element in den Cache ein.
     * @param command Der Befehl
     * @param result Das Ergebnis zum Befehl
     */
    public void put(String command, String result) {
        if (cache.size() <= Config.CACHE_SIZE) {
            cache.put(command, new CacheItem(command, result));
            return;
        }
        System.err.println("Cache is full. Command can't be cached.");
    }
    /**
     * Holt ein Element aus dem Cache.
     * @param command Der Befehl
     * @return Das Ergebnis zum Befehl
     */
    public String get(String command) {
        CacheItem item = cache.get(command);
        if (item != null) {
            item.setLastAccessedAt(System.currentTimeMillis());
        }
        return item != null ? item.getResult() : null;
    }
    /**
     * Löscht alle Elemente aus dem Cache, die älter als {@link Config#CLEAN_UP_INTERVAL} sind.
     */
    public void clean() {
        cache.forEach((command, cachedItem) -> {
            if (System.currentTimeMillis() - cachedItem.getLastAccessedAt() > Config.CLEAN_UP_INTERVAL) {
                cache.remove(command);
            }
        });
    }
    /**
     * Beendet den Cache.
     */
    public void shutdown() {
        cleanCacheTask.interrupt();
        cache.clear();
        System.err.println("CacheSystem was shut down.");
    }
}
