package com.letsplay.api.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitService
 *
 * Note: This is a pure unit test that does NOT require MongoDB or Spring context.
 */
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();

        // Configure rate limit: 3 requests per 1 minute
        ReflectionTestUtils.setField(rateLimitService, "capacity", 3);
        ReflectionTestUtils.setField(rateLimitService, "refillMinutes", 1L);
    }

    @Test
    void testInitialRequestsAreAllowed() {
        String clientId = "192.168.1.1";

        // First 3 requests should be allowed
        assertTrue(rateLimitService.tryConsume(clientId));
        assertTrue(rateLimitService.tryConsume(clientId));
        assertTrue(rateLimitService.tryConsume(clientId));
    }

    @Test
    void testExcessRequestsAreBlocked() {
        String clientId = "192.168.1.2";

        // Consume all tokens
        assertTrue(rateLimitService.tryConsume(clientId));
        assertTrue(rateLimitService.tryConsume(clientId));
        assertTrue(rateLimitService.tryConsume(clientId));

        // 4th request should be blocked
        assertFalse(rateLimitService.tryConsume(clientId));
        assertFalse(rateLimitService.tryConsume(clientId)); // Still blocked
    }

    @Test
    void testDifferentClientsHaveSeparateBuckets() {
        String client1 = "192.168.1.1";
        String client2 = "192.168.1.2";

        // Consume all tokens for client1
        assertTrue(rateLimitService.tryConsume(client1));
        assertTrue(rateLimitService.tryConsume(client1));
        assertTrue(rateLimitService.tryConsume(client1));
        assertFalse(rateLimitService.tryConsume(client1)); // Blocked

        // Client2 should still have tokens
        assertTrue(rateLimitService.tryConsume(client2));
        assertTrue(rateLimitService.tryConsume(client2));
        assertTrue(rateLimitService.tryConsume(client2));
    }

    @Test
    void testGetAvailableTokens() {
        String clientId = "192.168.1.3";

        // Initially should have 3 tokens
        assertEquals(3, rateLimitService.getAvailableTokens(clientId));

        // Consume one token
        rateLimitService.tryConsume(clientId);
        assertEquals(2, rateLimitService.getAvailableTokens(clientId));

        // Consume another
        rateLimitService.tryConsume(clientId);
        assertEquals(1, rateLimitService.getAvailableTokens(clientId));
    }

    @Test
    void testClearRateLimit() {
        String clientId = "192.168.1.4";

        // Consume all tokens
        rateLimitService.tryConsume(clientId);
        rateLimitService.tryConsume(clientId);
        rateLimitService.tryConsume(clientId);
        assertFalse(rateLimitService.tryConsume(clientId)); // Blocked

        // Clear rate limit
        rateLimitService.clearRateLimit(clientId);

        // Should be able to consume again
        assertTrue(rateLimitService.tryConsume(clientId));
    }

    @Test
    void testClearAllRateLimits() {
        String client1 = "192.168.1.5";
        String client2 = "192.168.1.6";

        // Consume tokens for both clients
        rateLimitService.tryConsume(client1);
        rateLimitService.tryConsume(client2);

        // Clear all
        rateLimitService.clearAllRateLimits();

        // Both should have full capacity again
        assertEquals(3, rateLimitService.getAvailableTokens(client1));
        assertEquals(3, rateLimitService.getAvailableTokens(client2));
    }

    @Test
    void testDifferentCapacity() {
        RateLimitService customService = new RateLimitService();
        ReflectionTestUtils.setField(customService, "capacity", 5);
        ReflectionTestUtils.setField(customService, "refillMinutes", 1L);

        String clientId = "192.168.1.7";

        // Should allow 5 requests
        for (int i = 0; i < 5; i++) {
            assertTrue(customService.tryConsume(clientId), "Request " + (i + 1) + " should be allowed");
        }

        // 6th should be blocked
        assertFalse(customService.tryConsume(clientId));
    }

    @Test
    void testResolveBucketCreatesNewBucketForNewClient() {
        String clientId = "192.168.1.8";

        assertNotNull(rateLimitService.resolveBucket(clientId));
        assertEquals(3, rateLimitService.getAvailableTokens(clientId));
    }

    @Test
    void testResolveBucketReturnsExistingBucketForKnownClient() {
        String clientId = "192.168.1.9";

        // Consume a token
        rateLimitService.tryConsume(clientId);
        assertEquals(2, rateLimitService.getAvailableTokens(clientId));

        // Resolve again - should get the same bucket with 2 tokens
        rateLimitService.resolveBucket(clientId);
        assertEquals(2, rateLimitService.getAvailableTokens(clientId));
    }
}
