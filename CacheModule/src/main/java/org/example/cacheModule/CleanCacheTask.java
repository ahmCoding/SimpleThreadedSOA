package org.example.cacheModule;

public class CleanCacheTask implements Runnable {
    private CacheSystem cache;

    public CleanCacheTask(CacheSystem cache) {
        this.cache = cache;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            cache.clean();
            try {
                Thread.sleep(Config.CLEAN_UP_DELAY);
            } catch (InterruptedException e) {
                System.err.println("CleanCacheTask is interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.err.println("CleanCacheTask shut down.");
    }
}
