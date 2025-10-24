package com.letsplay.api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service using Bucket4j
 *
 * Implements a token bucket algorithm to limit requests per client (identified by IP address).
 * Primarily used to prevent brute force attacks on authentication endpoints.
 */
@Service
public class RateLimitService {

    /**
     * Number of requests allowed within the time window
     */
    @Value("${ratelimit.capacity:5}")
    private int capacity;

    /**
     * Time window in minutes for rate limiting
     */
    @Value("${ratelimit.refill-minutes:1}")
    private long refillMinutes;

    /**
     * In-memory cache of buckets per client identifier (typically IP address)
     * In production, consider using a distributed cache like Redis
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolves a bucket for the given identifier (IP address)
     *
     * @param identifier Client identifier (IP address)
     * @return Bucket for the identifier
     */
    public Bucket resolveBucket(String identifier) {
        return cache.computeIfAbsent(identifier, k -> createNewBucket());
    }

    /**
     * Creates a new bucket with configured capacity and refill rate
     *
     * @return New bucket instance
     */
    private Bucket createNewBucket() {
        // Define bandwidth limit: capacity tokens, refilled at refillMinutes interval
        Bandwidth limit = Bandwidth.classic(
            capacity,
            Refill.intervally(capacity, Duration.ofMinutes(refillMinutes))
        );

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Attempts to consume a token from the bucket
     *
     * @param identifier Client identifier (IP address)
     * @return true if token was consumed (request allowed), false otherwise
     */
    public boolean tryConsume(String identifier) {
        Bucket bucket = resolveBucket(identifier);
        return bucket.tryConsume(1);
    }

    /**
     * Gets the number of available tokens for an identifier
     *
     * @param identifier Client identifier (IP address)
     * @return Number of available tokens
     */
    public long getAvailableTokens(String identifier) {
        Bucket bucket = resolveBucket(identifier);
        return bucket.getAvailableTokens();
    }

    /**
     * Clears the rate limit for a specific identifier (useful for testing)
     *
     * @param identifier Client identifier to clear
     */
    public void clearRateLimit(String identifier) {
        cache.remove(identifier);
    }

    /**
     * Clears all rate limits (useful for testing)
     */
    public void clearAllRateLimits() {
        cache.clear();
    }
}
