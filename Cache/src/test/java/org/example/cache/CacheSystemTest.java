package org.example.cache;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CacheSystemTest {
    private CacheSystem cacheSystem;

    @BeforeEach
    void setUp() {
        cacheSystem = new CacheSystem();
    }

    @AfterEach
    void tearDown() {
        cacheSystem.shutdown();
    }

    @Test
    void testPutAndGet() {
        // Given
        String command = "testCommand";
        String result = "testResult";

        // When
        cacheSystem.put(command, result);
        String cachedResult = cacheSystem.get(command);

        // Then
        assertEquals(result, cachedResult, "Der gecachte Wert sollte dem ursprünglichen Wert entsprechen");
    }

    @Test
    void testGetNonExistentItem() {
        // When
        String result = cacheSystem.get("nichtExistierenderBefehl");

        // Then
        assertNull(result, "Nicht existierende Items sollten null zurückgeben");
    }

    @Test
    void testCleanRemovesOldItems() throws InterruptedException {
        // Given
        cacheSystem.put("command1", "result1");

        // When
        // Warte länger als das Clean-Up-Interval
        Thread.sleep(Config.CLEAN_UP_INTERVAL + 100);
        // Then
        assertNull(cacheSystem.get("command1"), "Alte Items sollten nach dem Cleanup nicht mehr im Cache sein");
    }

    @Test
    void testCacheSizeLimit() {
        // Given
        int maxSize = Config.CACHE_SIZE;

        // When
        for (int i = 0; i <= maxSize + 1; i++) {
            cacheSystem.put("command" + i, "result" + i);
        }

        // Then
        assertNull(cacheSystem.get("command" + (maxSize + 1)),
                "Element über dem Größenlimit sollte nicht gecacht werden");
    }

    @Test
    void testItemUpdateLastAccessed() throws InterruptedException {
        // Given
        String command = "testCommand";
        String result = "testResult";
        cacheSystem.put(command, result);

        // When
        Thread.sleep(Config.CLEAN_UP_INTERVAL / 2);
        String firstAccess = cacheSystem.get(command);
        Thread.sleep(Config.CLEAN_UP_INTERVAL / 2);
        String secondAccess = cacheSystem.get(command);
        Thread.sleep(10_000);
        // Then
        assertNotNull(secondAccess,
                "Item sollte nach dem Cleanup noch existieren, da es zwischendurch zugegriffen wurde");
        assertEquals(result, secondAccess,
                "Der Wert sollte sich nach mehrmaligem Zugriff nicht ändern");
    }
}