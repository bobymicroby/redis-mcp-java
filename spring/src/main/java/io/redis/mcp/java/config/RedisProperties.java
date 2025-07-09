package io.redis.mcp.java.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Redis MCP integration.
 *
 * @author Borislav Ivanov
 */
@ConfigurationProperties(prefix = "redis.mcp")
public record RedisProperties(
    /**
     * Redis connection URL (e.g., "redis://localhost:6379")
     */
    String url,
    
    /**
     * Connection pool configuration
     */
    Pool pool
) {
    /**
     * Connection pool settings
     */
    public record Pool(
        /**
         * Maximum number of connections in the pool
         */
        int size
    ) {}
}