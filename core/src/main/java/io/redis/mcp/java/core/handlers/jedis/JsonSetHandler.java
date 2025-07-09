package io.redis.mcp.java.core.handlers.jedis;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.redis.mcp.java.core.net.Redis;
import io.redis.mcp.java.core.tooling.Description;
import io.redis.mcp.java.core.tooling.JedisHandler;
import io.redis.mcp.java.core.tooling.RedisHandler;
import io.redis.mcp.java.core.validation.Result;
import io.redis.mcp.java.core.validation.V;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path2;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Handler for Redis JSON operations in the Model Context Protocol (MCP) server.
 *
 * @author Borislav Ivanov
 */
public class JsonSetHandler extends JedisHandler {

    public JsonSetHandler(Redis redis) {
        super(redis);
    }

    @Override
    public McpSchema.Tool toolSchema() {
        return new McpSchema.Tool(
                "redis_json_set",
                "Set a JSON value at a specific key and path in Redis with optional conditional settings",
                recordToJSONSchema(JsonSetRequest.class));
    }

    public record JsonSetRequest(
            @Nonnull
            @Description("The Redis key to set")
            String key,

            @Nonnull
            @Description("The JSON value to set (as a JSON string)")
            String value,

            @Description("JSONPath to the location where to set the value (default: '$' for root)")
            String path,

            @Description("Only set if key does not exist")
            Boolean nx,

            @Description("Only set if key already exists")
            Boolean xx,

            @Description("Whether to escape special characters in the JSON value")
            Boolean escape
    ) {
    }

    @Override
    public McpSchema.CallToolResult handleSync(McpSyncServerExchange exchange, Map<String, Object> arguments) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return result.unwrapErr();
        }

        return handle(result.unwrap(), getConnection());
    }

    @Override
    public CompletableFuture<McpSchema.CallToolResult> handleAsync(McpAsyncServerExchange exchange, Map<String, Object> arguments) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return completedFuture(result.unwrapErr());
        }

        return getConnectionAsync().thenApply(connection -> handle(result.unwrap(), connection));
    }

    private McpSchema.CallToolResult handle(JsonSetRequest request, JedisPooled connection) {
        try {
            // Parse the JSON string to an object
            Object jsonValue = parseJsonValue(request.value);

            // Determine the path (default to root if not specified)
            Path2 jsonPath = request.path != null ? new Path2(request.path) : Path2.ROOT_PATH;

            // Build optional parameters
            JsonSetParams params = buildJsonSetParams(request);

            String response;
            if (params != null) {
                if (request.escape != null && request.escape) {
                    response = connection.jsonSetWithEscape(request.key, jsonPath, jsonValue, params);
                } else {
                    response = connection.jsonSet(request.key, jsonPath, jsonValue, params);
                }
            } else {
                if (request.escape != null && request.escape) {
                    response = connection.jsonSetWithEscape(request.key, jsonPath, jsonValue);
                } else {
                    response = connection.jsonSet(request.key, jsonPath, jsonValue);
                }
            }

            return generateSetResult(request.key, jsonPath.toString(), response);
        } catch (Exception e) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Error setting JSON value: " + e.getMessage())
                    .isError(true)
                    .build();
        }
    }


    private static Result<JsonSetRequest, McpSchema.CallToolResult> validateRequest(Map<String, Object> arguments) {
        return Result.combine(
                V.String(arguments, "key"),
                V.String(arguments, "value"),
                V.OptionalString(arguments, "path"),
                V.OptionalBoolean(arguments, "nx"),
                V.OptionalBoolean(arguments, "xx"),
                V.OptionalBoolean(arguments, "escape")
        ).with(JsonSetRequest::new);
    }

    private static Object parseJsonValue(String jsonString) {
        // Try to parse as different JSON types
        String trimmed = jsonString.trim();

        // Handle JSON strings (quoted values)
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed;  // Keep as is for JSON string values
        }

        // Handle JSON arrays
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }

        // Handle JSON objects
        if (trimmed.startsWith("{")) {
            return new JSONObject(trimmed);
        }

        // Handle boolean values
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.parseBoolean(trimmed);
        }

        // Handle null
        if ("null".equalsIgnoreCase(trimmed)) {
            return JSONObject.NULL;
        }

        // Try to parse as number
        try {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            } else {
                return Long.parseLong(trimmed);
            }
        } catch (NumberFormatException e) {
            // If all else fails, treat as a string
            return trimmed;
        }
    }

    private static JsonSetParams buildJsonSetParams(JsonSetRequest request) {
        boolean hasOptions = request.nx != null || request.xx != null;

        if (!hasOptions) {
            return null;
        }

        JsonSetParams params = new JsonSetParams();

        if (request.nx != null && request.nx) {
            params.nx();
        }
        if (request.xx != null && request.xx) {
            params.xx();
        }

        return params;
    }

    private static McpSchema.CallToolResult generateSetResult(String key, String path, String response) {
        if ("OK".equals(response)) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Successfully set JSON value at key '" + key + "' path '" + path + "'")
                    .isError(false)
                    .build();
        } else if (response == null) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("JSON value not set at key '" + key + "' (condition not met)")
                    .isError(false)
                    .build();
        } else {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Unexpected response: " + response)
                    .isError(true)
                    .build();
        }
    }
}