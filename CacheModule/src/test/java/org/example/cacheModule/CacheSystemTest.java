package org.example.cacheModule;

import org.example.command.Command;
import org.example.command.FakeCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Command command = new FakeCommand("testCommand");
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
        Command command = new FakeCommand("nichtExistierenderBefehl");
        String result = cacheSystem.get(command);

        // Then
        assertNull(result, "Nicht existierende Items sollten null zurückgeben");
    }

    @Test
    void testCleanRemovesOldItems() throws InterruptedException {
        // Given
        Command command = new FakeCommand("command1");
        cacheSystem.put(command, "result1");

        // When
        // Warte länger als das Clean-Up-Interval
        Thread.sleep(Config.CLEAN_UP_INTERVAL + 100);
        // Then
        assertNull(cacheSystem.get(command), "Alte Items sollten nach dem Cleanup nicht mehr im Cache sein");
    }

    @Test
    void testCacheSizeLimit() {
        // Given
        int maxSize = Config.CACHE_SIZE;

        // When
        for (int i = 0; i <= maxSize + 1; i++) {
            Command command = new FakeCommand("command" + i);
            cacheSystem.put(command, "result" + i);
        }

        // Then
        assertNull(cacheSystem.get(new FakeCommand("command" + (maxSize + 1))),
                "Element über dem Größenlimit sollte nicht gecacht werden");
    }

    @Test
    void testItemUpdateLastAccessed() throws InterruptedException {
        // Given
        FakeCommand command = new FakeCommand("testCommand");
        String result = "testResult";
        cacheSystem.put(command, result);

        // When
        Thread.sleep(Config.CLEAN_UP_INTERVAL / 2);
        cacheSystem.get(command); // First access
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