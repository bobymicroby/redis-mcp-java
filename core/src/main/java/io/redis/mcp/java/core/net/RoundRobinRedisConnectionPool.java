package io.redis.mcp.java.core.net;

import io.lettuce.core.api.StatefulRedisConnection;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Round-robin connection pool for Redis connections.
 * Manages a fixed set of connection IDs and distributes them in round-robin fashion.
 *
 * 
 */
public class RoundRobinRedisConnectionPool<T> {


    public interface ConnectionProvider<T> {

        CompletableFuture<T> getConnection(
            ConnectionHandle connectionHandle
        );
    }

    private static final int DEFAULT_POOL_SIZE = 4;

    private final List<String> connectionIds;
    private final AtomicInteger counter;
    private final ConnectionProvider<T> connectionsCache;
    private final String redisUri;

    /**
     * Creates a new round-robin connection pool with default size (4 connections).
     *
     * @param connectionCache the underlying connection cache
     * @param redisUri        the Redis URI to connect to
     */
    public RoundRobinRedisConnectionPool(
        String redisUri,
        ConnectionProvider<T> connectionCache
    ) {
        this(redisUri, connectionCache, DEFAULT_POOL_SIZE);
    }

    /**
     * Creates a new round-robin connection pool with specified size.
     *
     * @param connectionsCache the underlying connection cache
     * @param poolSize         the number of connection IDs to maintain
     */
    public RoundRobinRedisConnectionPool(
        String redisUri,
        ConnectionProvider<T> connectionsCache,
        int poolSize
    ) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be positive");
        }

        this.connectionsCache = connectionsCache;
        this.counter = new AtomicInteger(0);
        this.connectionIds = generateConnectionIds(poolSize);
        this.redisUri = redisUri;
    }

    /**
     * Gets the next connection in round-robin fashion.
     * Thread-safe and guaranteed to distribute connections evenly.
     *
     * @return a CompletableFuture containing the Redis connection
     */
    public CompletableFuture<T> getConnectionAsync() {
        String connectionId = getNextConnectionId();
        return connectionsCache.getConnection(
            new ConnectionHandle(redisUri, connectionId)
        );
    }

    /**
     * Gets the next connection ID in round-robin fashion.
     * Useful if you need the ID for logging or debugging.
     *
     * @return the next connection ID
     */
    public String getNextConnectionId() {
        int index = counter.getAndIncrement() % connectionIds.size();
        return connectionIds.get(index);
    }

    /**
     * Gets the current pool size.
     *
     * @return the number of connection IDs in the pool
     */
    public int getPoolSize() {
        return connectionIds.size();
    }

    /**
     * Gets an immutable view of all connection IDs.
     *
     * @return unmodifiable list of connection IDs
     */
    public List<String> getConnectionIds() {
        return List.copyOf(connectionIds);
    }

    /**
     * Generates a list of unique UUID-based connection IDs.
     */
    private static List<String> generateConnectionIds(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> UUID.randomUUID().toString())
            .toList();
    }
}
