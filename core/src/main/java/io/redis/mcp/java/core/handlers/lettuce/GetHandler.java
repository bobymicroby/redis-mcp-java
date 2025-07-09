package io.redis.mcp.java.core.handlers.lettuce;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.redis.mcp.java.core.net.Redis;
import io.redis.mcp.java.core.tooling.Description;
import io.redis.mcp.java.core.tooling.LettuceHandler;
import io.redis.mcp.java.core.validation.Result;
import io.redis.mcp.java.core.validation.V;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Handler for Redis GET operations in the Model Context Protocol (MCP) server.
 *
 * @author Borislav Ivanov
 */
public class GetHandler extends LettuceHandler {


    public GetHandler(Redis redis) {
        super(redis);
    }

    public record GetRequest(
            @Nonnull
            @Description("The Redis key to retrieve") String key
    ) {
    }

    @Override
    public McpSchema.Tool toolSchema() {
        return new McpSchema.Tool(
                "redis_get",
                "Retrieve a value from Redis by key. Returns the value associated with the key, or null if the key doesn't exist.",
                recordToJSONSchema(GetRequest.class)
        );
    }

    @Override
    public McpSchema.CallToolResult handleSync(
            McpSyncServerExchange exchange,
            Map<String, Object> arguments
    ) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return result.unwrapErr();
        }

        var key = result.unwrap().key;
        var syncConnection = getConnection().sync();
        var value = syncConnection.get(key);

        return generateResult(key, value);
    }

    public CompletableFuture<McpSchema.CallToolResult> handleAsync(
            McpAsyncServerExchange exchange,
            Map<String, Object> arguments
    ) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return CompletableFuture.completedFuture(result.unwrapErr());
        }

        var key = result.unwrap().key;

        this.getConnectionAsync();

        return this.getConnectionAsync()
                .thenCompose(connection -> connection.async().get(key))
                .thenApply(value -> generateResult(key, value));
    }

    private static Result<GetRequest, McpSchema.CallToolResult> validateRequest(
            Map<String, Object> arguments
    ) {
        return Result.combine(V.String(arguments, "key")).with(GetRequest::new);
    }

    private static McpSchema.CallToolResult generateResult(
            String key,
            String value
    ) {
        if (value == null) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Key '" + key + "' not found in Redis")
                    .isError(false)
                    .build();
        } else {
            return McpSchema.CallToolResult.builder()
                    .addTextContent(value)
                    .isError(false)
                    .build();
        }
    }
}
