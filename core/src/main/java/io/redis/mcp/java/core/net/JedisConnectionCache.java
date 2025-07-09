package io.redis.mcp.java.core.net;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;


import java.util.concurrent.CompletableFuture;

/**
 * A thread-safe, asynchronous connection cache for Redis connections that provides automatic
 * connection lifecycle management with configurable size limits.
 *
 * <p>This cache creates Redis connections on-demand when requested with a {@link ConnectionHandle}.
 * Connections are automatically created, cached for reuse, and properly closed when evicted due to
 * cache size constraints or explicit invalidation.</p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><b>On-demand creation:</b> Connections are created lazily when first requested</li>
 *   <li><b>Automatic cleanup:</b> Connections are automatically closed when evicted from cache</li>
 *   <li><b>Size-bounded:</b> Cache respects maximum connection limits to prevent resource exhaustion</li>
 *   <li><b>Thread-safe:</b> All operations are safe for concurrent access</li>
 *   <li><b>Asynchronous:</b> All operations return CompletableFuture for non-blocking usage</li>
 *   <li><b>Flexible sizing:</b> Works equally well with cache size of 1 (single connection with automatic management) or larger sizes for connection pooling</li>
 * </ul>
 *
 * <h3>Connection Lifecycle:</h3>
 * <ol>
 *   <li>Client requests connection via {@link #getConnection(ConnectionHandle)}</li>
 *   <li>If connection exists in cache, it's returned immediately</li>
 *   <li>If connection doesn't exist, it's created asynchronously using the URL and ID from ConnectionHandle</li>
 *   <li>Connection is cached for subsequent requests with the same ConnectionHandle</li>
 *   <li>When cache reaches maximum size, least-recently-used connections are evicted and closed</li>
 * </ol>
 *
 * @author Borislav Ivanov
 * @see ConnectionHandle
 */
public class JedisConnectionCache {


    public final AsyncLoadingCache<ConnectionHandle, JedisPooled> connectionCache;


    public JedisConnectionCache(int maxConnections) {

        RemovalListener<Object, Object> closeConnectionListener = (key, value, cause) -> {
            if (value != null && value instanceof JedisPooled) {
                JedisPooled conn = (JedisPooled) value;
                conn.close();
            }
        };
        connectionCache = Caffeine.newBuilder()
                .maximumSize(maxConnections)
                .removalListener(closeConnectionListener)
                .evictionListener(closeConnectionListener)
                .buildAsync((key) -> new JedisPooled(key.url()));

    }

    public CompletableFuture<JedisPooled> getConnection(ConnectionHandle id) {
        return connectionCache.get(id);

    }

    public void invalidateAll() {
        connectionCache.synchronous().invalidateAll();
    }

}
