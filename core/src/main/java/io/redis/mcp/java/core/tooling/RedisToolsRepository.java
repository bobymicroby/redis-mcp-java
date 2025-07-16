package io.redis.mcp.java.core.tooling;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.redis.mcp.java.core.net.Redis;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository for managing Redis MCP tool specifications and configurations.
 * <p>
 * This class serves as the main entry point for configuring and retrieving
 * Redis-based MCP tools.
 * <p>
 * <p>
 * Key responsibilities:
 * - Setting up Redis connection pools with appropriate sizing
 * - Discovering and instantiating handler classes via reflection
 * - Creating MCP tool specifications for server registration
 * - Supporting both sync and async execution patterns
 *
 * 
 * @see HandlerScanner
 * @see LettuceHandler
 * @see Redis.WithLazyConnectionPool
 */
public class RedisToolsRepository {

    public static final List<String> HANDLER_PACKAGES = List.of("io.redis.mcp.java.core.handlers.lettuce",
            "io.redis.mcp.java.core.handlers.jedis");

    /**
     * Creates asynchronous tool specifications for all discovered Redis handlers.
     * <p>
     * This method sets up a Redis connection pool and scans for handler implementations,
     * then creates MCP async tool specifications that can be registered with an MCP server.
     * Each tool specification includes the tool schema and an async execution handler.
     * </p>
     * <p>
     * The async specifications are suitable for non-blocking MCP server implementations
     * that need to handle high concurrency and reactive programming patterns.
     * </p>
     *
     * @param redisUrl       the Redis connection URL (e.g., "redis://localhost:6379")
     * @param maxConnections the maximum number of connections in the pool (currently unused, defaults to 1)
     * @param packages       the packages list to scan for handler implementations
     * @return a list of async tool specifications ready for MCP server registration
     * @throws RuntimeException         if Redis connection setup fails or handler discovery fails
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public static List<McpServerFeatures.AsyncToolSpecification> getAsyncToolSpecifications(String redisUrl, int maxConnections, List<String> packages) {
        var redis = new Redis.WithLazyConnectionPool(redisUrl, maxConnections);

        Map<String, RedisHandler> flattenedHandlers = HandlerScanner.instantiateHandlers(redis, packages);

        return flattenedHandlers.entrySet().stream().map(entry -> {

            RedisHandler handler = entry.getValue();
            Tool tool = handler.toolSchema();

            return new McpServerFeatures.AsyncToolSpecification(tool,
                    (e, a) -> Mono.fromFuture(handler.handleAsync(e, a)));
        }).toList();
    }


    public static List<McpServerFeatures.AsyncToolSpecification> getAsyncToolSpecifications(String redisUrl, int maxConnections) {
        return getAsyncToolSpecifications(redisUrl, maxConnections, HANDLER_PACKAGES);
    }

    /**
     * Creates synchronous tool specifications for all discovered Redis handlers.
     * <p>
     * This method sets up a Redis connection pool and scans for handler implementations,
     * then creates MCP sync tool specifications that can be registered with an MCP server.
     * Each tool specification includes the tool schema and a synchronous execution handler.
     * </p>
     * <p>
     * The sync specifications are suitable for blocking MCP server implementations
     * that prefer simpler programming models and thread-per-request architectures.
     * </p>
     *
     * @param redisUrl       the Redis connection URL (e.g., "redis://localhost:6379")
     * @param maxConnections the maximum number of connections in the pool (currently unused, defaults to 1)
     * @param packages       the packages to scan for handler implementations
     * @return a list of sync tool specifications ready for MCP server registration
     * @throws RuntimeException         if Redis connection setup fails or handler discovery fails
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public static List<McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications(String redisUrl, int maxConnections, List<String> packages) {
        var redis = new Redis.WithLazyConnectionPool(redisUrl, maxConnections);

        return HandlerScanner.instantiateHandlers(redis, packages).entrySet().stream().map(entry -> {
            RedisHandler handler = entry.getValue();
            Tool tool = handler.toolSchema();
            return new McpServerFeatures.SyncToolSpecification(tool, handler::handleSync);
        }).toList();
    }

    public static List<McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications(String redisUrl, int maxConnections) {

        return getSyncToolSpecifications(redisUrl, maxConnections, HANDLER_PACKAGES);
    }

}
