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
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Handler for Redis JSON GET operations in the Model Context Protocol (MCP) server.
 *
 * 
 */
public class JsonGetHandler extends JedisHandler {

    public JsonGetHandler(Redis redis) {
        super(redis);
    }

    public record JsonGetRequest(
            @Description("Single Redis key to retrieve JSON from (use either 'key' or 'keys', not both)")
            String key,

            @Description("Multiple Redis keys for JSON multi-get (use either 'key' or 'keys', not both)")
            List<String> keys,

            @Description("JSONPath expressions to retrieve specific parts of the JSON (default: '$' for root)")
            List<String> paths
    ) {
    }

    @Override
    public McpSchema.Tool toolSchema() {
        return new McpSchema.Tool(
                "redis_json_get",
                "Retrieve JSON values from Redis by key(s) and optional JSONPath expressions. " +
                        "Supports both single key get with multiple paths and multi-key get with a single path.",
                recordToJSONSchema(JsonGetRequest.class)
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

        var request = result.unwrap();
        JedisPooled connection = getConnection();

        try {
            // Single key get
            if (request.key != null) {
                return handleSingleKeyGet(connection, request.key, request.paths);
            }
            // Multi-key get
            else if (request.keys != null && !request.keys.isEmpty()) {
                return handleMultiKeyGet(connection, request.keys, request.paths);
            } else {
                return McpSchema.CallToolResult.builder()
                        .addTextContent("Either 'key' or 'keys' must be provided")
                        .isError(true)
                        .build();
            }
        } catch (Exception e) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Error retrieving JSON value: " + e.getMessage())
                    .isError(true)
                    .build();
        }
    }

    @Override
    public CompletableFuture<McpSchema.CallToolResult> handleAsync(
            McpAsyncServerExchange exchange,
            Map<String, Object> arguments
    ) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return completedFuture(result.unwrapErr());
        }

        var request = result.unwrap();

        return CompletableFuture.supplyAsync(() -> {
            JedisPooled connection = getConnection();

            try {
                // Single key get
                if (request.key != null) {
                    return handleSingleKeyGet(connection, request.key, request.paths);
                }
                // Multi-key get
                else if (request.keys != null && !request.keys.isEmpty()) {
                    return handleMultiKeyGet(connection, request.keys, request.paths);
                } else {
                    return McpSchema.CallToolResult.builder()
                            .addTextContent("Either 'key' or 'keys' must be provided")
                            .isError(true)
                            .build();
                }
            } catch (Exception e) {
                return McpSchema.CallToolResult.builder()
                        .addTextContent("Error retrieving JSON value: " + e.getMessage())
                        .isError(true)
                        .build();
            }
        });
    }

    private static Result<JsonGetRequest, McpSchema.CallToolResult> validateRequest(
            Map<String, Object> arguments
    ) {
        // Check that only one of 'key' or 'keys' is provided
        String key = (String) arguments.get("key");
        List<String> keys = (List<String>) arguments.get("keys");


        if (key != null && (keys != null && !keys.isEmpty())) {
            return Result.err(McpSchema.CallToolResult.builder()
                    .addTextContent("Cannot specify both 'key' and 'keys'. Use one or the other.")
                    .isError(true)
                    .build());
        }

        return Result.combine(
                V.OptionalString(arguments, "key"),
                V.OptionalList(arguments, "keys", String.class),
                V.OptionalList(arguments, "paths", String.class)
        ).with(JsonGetRequest::new);
    }

    private McpSchema.CallToolResult handleSingleKeyGet(
            JedisPooled connection,
            String key,
            List<String> paths
    ) {
        Object value;

        if (paths == null || paths.isEmpty()) {
            // Get entire JSON document
            value = connection.jsonGet(key);
        } else {
            // Get specific paths
            Path2[] pathArray = paths.stream()
                    .map(Path2::new)
                    .toArray(Path2[]::new);
            value = connection.jsonGet(key, pathArray);
        }

        return generateSingleKeyResult(key, value, paths);
    }

    private McpSchema.CallToolResult handleMultiKeyGet(
            JedisPooled connection,
            List<String> keys,
            List<String> paths
    ) {
        List<JSONArray> values;
        String[] keyArray = keys.toArray(new String[0]);

        if (paths == null || paths.isEmpty() || paths.size() == 1) {
            // Use default root path or single specified path
            Path2 path = (paths == null || paths.isEmpty())
                    ? Path2.ROOT_PATH
                    : new Path2(paths.get(0));
            values = connection.jsonMGet(path, keyArray);
        } else {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Multi-key get (mget) only supports a single path")
                    .isError(true)
                    .build();
        }

        return generateMultiKeyResult(keys, values, paths);
    }

    private static McpSchema.CallToolResult generateSingleKeyResult(
            String key,
            Object value,
            List<String> paths
    ) {
        if (value == null) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Key '" + key + "' not found in Redis")
                    .isError(false)
                    .build();
        }

        StringBuilder result = new StringBuilder();
        result.append("JSON value(s) for key '").append(key).append("'");

        if (paths != null && !paths.isEmpty()) {
            result.append(" at path(s) ").append(paths).append("");
        }
        result.append(":\n");

        // Format the JSON output
        if (value instanceof String) {
            result.append(value);
        } else {
            result.append(value.toString());
        }

        return McpSchema.CallToolResult.builder()
                .addTextContent(result.toString())
                .isError(false)
                .build();
    }

    private static McpSchema.CallToolResult generateMultiKeyResult(
            List<String> keys,
            List<JSONArray> values,
            List<String> paths
    ) {
        StringBuilder result = new StringBuilder();
        result.append("JSON multi-get results");

        if (paths != null && !paths.isEmpty()) {
            result.append(" at path '").append(paths.get(0)).append("'");
        }
        result.append(":\n");

        for (int i = 0; i < keys.size(); i++) {
            result.append("\n").append(keys.get(i)).append(": ");

            if (i < values.size() && values.get(i) != null) {
                JSONArray valueArray = values.get(i);
                if (valueArray.length() > 0 && !valueArray.isNull(0)) {
                    result.append(valueArray.get(0).toString());
                } else {
                    result.append("(nil)");
                }
            } else {
                result.append("(nil)");
            }
        }

        return McpSchema.CallToolResult.builder()
                .addTextContent(result.toString())
                .isError(false)
                .build();
    }
}