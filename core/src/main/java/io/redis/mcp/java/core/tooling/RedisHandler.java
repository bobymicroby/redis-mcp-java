package io.redis.mcp.java.core.tooling;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.victools.jsonschema.generator.Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT;
import static com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON;
import static com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_2020_12;

public interface RedisHandler {

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
    McpSchema.Tool toolSchema();

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
    McpSchema.CallToolResult handleSync(McpSyncServerExchange exchange, Map<String, Object> arguments);

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
    CompletableFuture<McpSchema.CallToolResult> handleAsync(McpAsyncServerExchange exchange, Map<String, Object> arguments);

    /**
     * Converts a Java record class to a JSON Schema string for MCP tool definitions.
     * <p>
     * This utility method generates JSON Schema from Java record classes, automatically:
     * - Extracting field descriptions from {@link Description} annotations
     * - Setting required fields based on {@link Nonnull} annotations
     * - Applying JSON Schema validation rules
     * - Formatting the schema for MCP tool consumption
     * </p>
     * <p>
     * The generated schema follows JSON Schema Draft 2020-12 specification and
     * includes additional properties restrictions for security.
     * </p>
     *
     * @param c the record class to convert to JSON schema
     * @return a JSON Schema string representing the record structure
     * @throws IllegalArgumentException if the class is not a valid record
     */
    default String recordToJSONSchema(Class<?> c) {

        var configBuilder = new SchemaGeneratorConfigBuilder(DRAFT_2020_12, PLAIN_JSON)
                .with(FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT).without(Option.SCHEMA_VERSION_INDICATOR);

        configBuilder.forTypesInGeneral().withPropertySorter((o1, o2) -> 0);
        configBuilder.forFields().withDescriptionResolver(field -> {

                    Annotation annotation = field.getAnnotation(Description.class);
                    if (annotation != null) {
                        Description description = (Description) annotation;
                        return description.value();
                    }

                    return null;

                })
                .withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(Nonnull.class) != null);


        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
        JsonNode jsonSchema = generator.generateSchema(c);

        return jsonSchema.toPrettyString();
    }
}
