package com.example.analytics.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class TopBooksCacheService {

    public static final String KEY_TOP_BOOKS = "analytics:top-books";
    private static final Duration KEY_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public TopBooksCacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void incrementBorrowCount(Long bookId) {
        redis.opsForZSet().incrementScore(KEY_TOP_BOOKS, String.valueOf(bookId), 1.0);
        redis.expire(KEY_TOP_BOOKS, KEY_TTL);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopBooks(int limit) {
        return redis.opsForZSet().reverseRangeWithScores(KEY_TOP_BOOKS, 0, Math.max(0, limit - 1));
    }
}
