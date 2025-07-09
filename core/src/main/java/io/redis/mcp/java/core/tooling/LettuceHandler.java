package io.redis.mcp.java.core.tooling;


import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.redis.mcp.java.core.net.Redis;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.victools.jsonschema.generator.Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT;
import static com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON;
import static com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_2020_12;


/**
 * Abstract base class for Redis Lettuce command handlers in the Model Context Protocol (MCP) server.
 * <p>
 * This class provides the foundation for implementing Redis operations as MCP tools.
 * It manages Redis connections and provides utilities for JSON schema generation
 * from Java records. Subclasses implement specific Redis commands by extending
 * this class and implementing the required abstract methods.
 * </p>
 * <p>
 * Key features:
 * - Connection management with sync, async, and reactive patterns
 * - Automatic JSON schema generation for MCP tool definitions
 * - Integration with Lettuce Redis client
 * - Support for both blocking and non-blocking operations
 * </p>
 *
 * @author Borislav Ivanov
 */
public abstract class LettuceHandler implements RedisHandler {

    /**
     * The Redis connection provider used by this handler.
     */
    private final Redis redis;

    /**
     * Constructs a new LettuceHandler with the specified Redis connection provider.
     *
     * @param redis the Redis connection provider to use for operations
     */
    public LettuceHandler(Redis redis) {
        this.redis = redis;
    }

    /**
     * Returns the MCP tool schema for this Redis operation.
     * <p>
     * Implementations must provide a complete tool schema that defines:
     * - Tool name and description
     * - Input parameter schema (typically generated from a record)
     * - Parameter validation rules
     * </p>
     *
     * @return the MCP tool schema for this handler
     */
    public abstract McpSchema.Tool toolSchema();

    /**
     * Handles synchronous execution of the Redis operation.
     * <p>
     * Implementations should:
     * - Validate input arguments
     * - Execute the Redis command using blocking operations
     * - Return appropriate success or error results
     * </p>
     *
     * @param exchange  the MCP server exchange context
     * @param arguments the tool invocation arguments
     * @return the result of the Redis operation
     */
    public abstract McpSchema.CallToolResult handleSync(McpSyncServerExchange exchange, Map<String, Object> arguments);

    /**
     * Handles asynchronous execution of the Redis operation.
     * <p>
     * Implementations should:
     * - Validate input arguments
     * - Execute the Redis command using reactive operations
     * - Return a Mono containing the operation result
     * </p>
     *
     * @param exchange  the MCP async server exchange context
     * @param arguments the tool invocation arguments
     * @return a Mono emitting the result of the Redis operation
     */
    public abstract CompletableFuture<McpSchema.CallToolResult> handleAsync(McpAsyncServerExchange exchange, Map<String, Object> arguments);


    /**
     * Gets a Redis connection using blocking operations.
     * <p>
     * This method blocks until a connection is available. For non-blocking
     * operations, use {@link #getConnectionAsync()} or {@link #getConnectionMono()}.
     * </p>
     *
     * @return a stateful Redis connection
     * @throws RuntimeException if connection acquisition fails
     */
    public StatefulRedisConnection<String, String> getConnection() {
        try {
            return getConnectionAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a Redis connection asynchronously.
     * <p>
     * Returns a CompletableFuture that will complete with a Redis connection
     * when one becomes available from the connection pool.
     * </p>
     *
     * @return a CompletableFuture containing a stateful Redis connection
     */
    public CompletableFuture<StatefulRedisConnection<String, String>> getConnectionAsync() {
        return redis.getLettuceConnection();
    }

    /**
     * Gets a Redis connection as a reactive Mono.
     * <p>
     * Returns a Mono that emits a Redis connection when available.
     * This is useful for reactive programming patterns and integration
     * with other reactive components.
     * </p>
     *
     * @return a Mono emitting a stateful Redis connection
     */
    public Mono<StatefulRedisConnection<String, String>> getConnectionMono() {
        return Mono.fromFuture(getConnectionAsync());
    }


}
