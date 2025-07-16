package io.redis.mcp.java.core.net;

import io.lettuce.core.api.StatefulRedisConnection;
import redis.clients.jedis.JedisPooled;

import java.util.concurrent.CompletableFuture;


/**
 * Interface for Redis connection providers in the MCP server.
 * <p>
 * This interface abstracts Redis connection management, allowing different
 * implementations for various deployment scenarios (single instance, cluster,
 * connection pooling, etc.).
 * </p>
 * <p>
 * Implementations must provide asynchronous connection acquisition to support
 * both blocking and non-blocking usage patterns in the MCP server.
 * </p>
 *
 * 
 */
public interface Redis {

    /**
     * Asynchronously acquires a Redis connection.
     * <p>
     * Returns a CompletableFuture that completes with a stateful Redis connection
     * when one becomes available. The connection should be thread-safe and
     * suitable for concurrent operations.
     * </p>
     *
     * @return a CompletableFuture containing a stateful Redis connection
     */
    CompletableFuture<StatefulRedisConnection<String, String>> getLettuceConnection();

    CompletableFuture<JedisPooled> getJedisConnection();


    /**
     * Default implementation using a lazy connection pool with round-robin distribution.
     * <p>
     * This implementation provides:
     * - Lazy connection initialization (connections created on-demand)
     * - Round-robin load balancing across pooled connections
     * - Connection caching for improved performance
     * - Automatic connection management and cleanup
     * </p>
     * <p>
     * </p>
     */
    class WithLazyConnectionPool implements Redis {


        private final RoundRobinRedisConnectionPool<StatefulRedisConnection<String, String>> lettucePool;
        private final RoundRobinRedisConnectionPool<JedisPooled> jedisPool;

        /**
         * Constructs a new connection pool-based Redis provider.
         *
         * @param redisUrl the Redis connection URL (e.g., "redis://localhost:6379")
         * @param poolSize the maximum number of connections in the pool
         * @throws IllegalArgumentException if redisUrl is null or poolSize is invalid
         */
        public WithLazyConnectionPool(String redisUrl, int poolSize) {


            this.lettucePool = new RoundRobinRedisConnectionPool<>(redisUrl, new RoundRobinRedisConnectionPool.ConnectionProvider<>() {
                final LettuceConnectionCache lettuceConnectionCache = new LettuceConnectionCache(poolSize);

                @Override
                public CompletableFuture<StatefulRedisConnection<String, String>> getConnection(ConnectionHandle connectionHandle) {
                    return lettuceConnectionCache.getConnection(connectionHandle);
                }
            }, poolSize);
            this.jedisPool = new RoundRobinRedisConnectionPool<>(redisUrl, new RoundRobinRedisConnectionPool.ConnectionProvider<>() {
                final JedisConnectionCache jedisConnectionCache = new JedisConnectionCache(poolSize);

                @Override
                public CompletableFuture<JedisPooled> getConnection(ConnectionHandle connectionHandle) {
                    return jedisConnectionCache.getConnection(connectionHandle);
                }
            }, poolSize);
        }


        /**
         * {@inheritDoc}
         * <p>
         * This implementation uses the underlying connection pool to provide
         * connections in a round-robin fashion, ensuring load distribution
         * across available connections.
         * </p>
         */
        public CompletableFuture<StatefulRedisConnection<String, String>> getLettuceConnection() {
            return lettucePool.getConnectionAsync();
        }

        /**
         * {@inheritDoc}
         * <p>
         * This implementation uses the underlying connection pool to provide
         * connections in a round-robin fashion, ensuring load distribution
         * across available connections.
         * </p>
         */
        @Override
        public CompletableFuture<JedisPooled> getJedisConnection() {
            return jedisPool.getConnectionAsync();
        }
    }
}
