package io.redis.mcp.java.core.tooling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for providing field descriptions in MCP tool schemas.
 * <p>
 * This annotation is used to add human-readable descriptions to record fields
 * and components, which are then automatically extracted during JSON schema
 * generation for MCP tool definitions. The descriptions help clients understand
 * the purpose and expected values for each parameter.
 * </p>
 * <p>
 * Usage example:
 * <pre>{@code
 * public record MyRequest(
 *     @Description("The Redis key to operate on")
 *     String key,
 *     
 *     @Description("Optional timeout in seconds")
 *     Long timeout
 * ) {}
 * }</pre>
 * </p>
 * <p>
 * The annotation is processed by {@link LettuceHandler#recordToJSONSchema(Class)}
 * during schema generation. 
 * </p>
 *
 * 
 * @see LettuceHandler#recordToJSONSchema(Class)
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    /**
     * The human-readable description of the annotated field.
     * <p>
     * This value should be concise but descriptive, explaining the purpose,
     * expected format, or constraints of the field. It will be included in
     * the generated JSON schema as the "description" property.
     * </p>
     *
     * @return the field description text
     */
    String value();
}
