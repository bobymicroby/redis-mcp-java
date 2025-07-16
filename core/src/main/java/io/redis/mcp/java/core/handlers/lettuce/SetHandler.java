package io.redis.mcp.java.core.handlers.lettuce;

import io.lettuce.core.SetArgs;
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

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Handler for Redis SET operations in the Model Context Protocol (MCP) server.
 *
 * 
 * 
 */
public class SetHandler extends LettuceHandler {

    
    public SetHandler(Redis redis) {
        super(redis);
    }

    @Override
    public McpSchema.Tool toolSchema() {

        return new McpSchema.Tool(
                "redis_set",
                "Set a key-value pair in Redis with optional expiration and conditional settings",
                recordToJSONSchema(SetRequest.class));
    }

    public record SetRequest(

            @Nonnull
            @Description("The Redis key to set")
            String key,

            @Nonnull
            @Description("The value to set")
            String value,

            @Description("Set expire time in seconds")
            Long ex,

            @Description("Set expire time in milliseconds")
            Long px,

            @Description("Set expire at UNIX timestamp (seconds)")
            Long exat,

            @Description("Set expire at UNIX timestamp (milliseconds)")
            Long pxat,

            @Description("Only set if key does not exist")
            Boolean nx,

            @Description("Only set if key already exists")
            Boolean xx,

            @Description("Retain existing TTL")
            Boolean keepttl,

            @Description("Return the old value stored at key")
            Boolean get
    ) {
    }


    @Override
    public McpSchema.CallToolResult handleSync(McpSyncServerExchange exchange, Map<String, Object> arguments) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return result.unwrapErr();
        }

        var request = result.unwrap();
        var syncConnection = getConnection().sync();


        if (request.get != null && request.get) {
            SetArgs setArgs = buildSetArgs(request);
            String oldValue;

            if (setArgs != null) {
                oldValue = syncConnection.setGet(request.key, request.value, setArgs);
            } else {
                oldValue = syncConnection.setGet(request.key, request.value);
            }

            return generateGetResult(request.key, oldValue);
        }


        SetArgs setArgs = buildSetArgs(request);
        String response;

        if (setArgs != null) {
            response = syncConnection.set(request.key, request.value, setArgs);
        } else {
            response = syncConnection.set(request.key, request.value);
        }

        return generateSetResult(request.key, response);
    }

    public CompletableFuture<McpSchema.CallToolResult> handleAsync(McpAsyncServerExchange exchange, Map<String, Object> arguments) {
        var result = validateRequest(arguments);

        if (result.isErr()) {
            return completedFuture(result.unwrapErr());
        }

        var request = result.unwrap();

        return this.getConnectionAsync()
                .thenCompose(connection -> {

                    if (request.get != null && request.get) {
                        SetArgs setArgs = buildSetArgs(request);
                        if (setArgs != null) {
                            return connection.async().setGet(request.key, request.value, setArgs);

                        } else {
                            return connection.async().setGet(request.key, request.value);
                        }
                    }


                    SetArgs setArgs = buildSetArgs(request);
                    if (setArgs != null) {
                        return connection.async().set(request.key, request.value, setArgs);
                    } else {
                        return connection.async().set(request.key, request.value);
                    }
                })

                .thenApply(response -> {

                    if (request.get != null && request.get) {
                        return generateGetResult(request.key, response);
                    } else {
                        return generateSetResult(request.key, response);
                    }
                });
    }

    private static Result<SetRequest, McpSchema.CallToolResult> validateRequest(Map<String, Object> arguments) {
        return Result.combine(
                V.String(arguments, "key"),
                V.String(arguments, "value"),
                V.OptionalLong(arguments, "ex"),
                V.OptionalLong(arguments, "px"),
                V.OptionalLong(arguments, "exat"),
                V.OptionalLong(arguments, "pxat"),
                V.OptionalBoolean(arguments, "nx"),
                V.OptionalBoolean(arguments, "xx"),
                V.OptionalBoolean(arguments, "keepttl"),
                V.OptionalBoolean(arguments, "get")
        ).with(SetRequest::new);
    }

    private static SetArgs buildSetArgs(SetRequest request) {
        boolean hasOptions = request.ex != null || request.px != null ||
                request.exat != null || request.pxat != null ||
                request.nx != null || request.xx != null ||
                request.keepttl != null;

        if (!hasOptions) {
            return null;
        }

        SetArgs args = new SetArgs();

        if (request.ex != null) {
            args.ex(request.ex);
        }
        if (request.px != null) {
            args.px(request.px);
        }
        if (request.exat != null) {
            args.exAt(request.exat);
        }
        if (request.pxat != null) {
            args.pxAt(request.pxat);
        }
        if (request.nx != null && request.nx) {
            args.nx();
        }
        if (request.xx != null && request.xx) {
            args.xx();
        }
        if (request.keepttl != null && request.keepttl) {
            args.keepttl();
        }

        return args;
    }

    private static McpSchema.CallToolResult generateSetResult(String key, String response) {
        if ("OK".equals(response)) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Successfully set key '" + key + "'"  )
                    .isError(false)
                    .build();
        } else if (response == null) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Key '" + key + "' not set (condition not met)")
                    .isError(false)
                    .build();
        } else {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Unexpected response: " + response)
                    .isError(true)
                    .build();
        }
    }

    private static McpSchema.CallToolResult generateGetResult(String key, String oldValue) {
        if (oldValue == null) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Key '" + key + "' set successfully. Previous value: (nil)")
                    .isError(false)
                    .build();
        } else {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Key '" + key + "' set successfully. Previous value: " + oldValue)
                    .isError(false)
                    .build();
        }
    }
}