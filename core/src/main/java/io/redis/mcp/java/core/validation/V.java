package io.redis.mcp.java.core.validation;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Validation utility class for Model Context Protocol (MCP) schema parameters.
 *
 * <p>This class provides a set of static methods for validating and extracting
 * parameters from a Map of arguments. It supports validation for common data types
 * including String, Long, and Boolean values, with both required and optional variants.</p>
 *
 * <p>All validation methods return a {@link Result} object that encapsulates either
 * a successful validation with the extracted value, or an error with an appropriate
 * {@link McpSchema.CallToolResult} error message.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Map&lt;String, Object&gt; arguments = ...;
 * Result&lt;String, McpSchema.CallToolResult&gt; nameResult = V.String(arguments, "name");
 * if (nameResult.isOk()) {
 *     String name = nameResult.unwrap();
 *     // Use the validated name
 * } else {
 *     // Handle validation error
 *     McpSchema.CallToolResult error = nameResult.unwrapErr();
 * }
 * </pre>
 *
 * @author Borislav Ivanov
 */
public class V {

    /**
     * Validates that a required parameter exists in the arguments map.
     *
     * <p>This method checks if the specified parameter exists in the arguments map
     * and is not null. If the parameter is missing or null, an error result is returned.</p>
     *
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the parameter to validate
     * @return a Result containing the parameter value if present, or an error if missing or null
     */
    public static Result<Object, McpSchema.CallToolResult> required(
            Map<String, Object> arguments,
            String paramName
    ) {

        return V.optional(arguments, paramName).andThen(x -> {
            if (x == null) {
                return Result.err(McpSchema.CallToolResult.builder()
                        .addTextContent(
                                paramName +
                                        " parameter is required and cannot be empty"
                        )
                        .isError(true)
                        .build());
            }
            return Result.ok(x);
        });
    }

    /**
     * Validates a required String parameter.
     *
     * <p>This method validates that:
     * <ul>
     *   <li>The parameter exists in the arguments map</li>
     *   <li>The parameter value is a String</li>
     *   <li>The String is not empty</li>
     * </ul>
     * </p>
     *
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the String parameter to validate
     * @return a Result containing the validated String value, or an error if validation fails
     */
    public static Result<String, McpSchema.CallToolResult> String(
            Map<String, Object> arguments,
            String paramName
    ) {
        return required(arguments, paramName).andThen(validateString(paramName));
    }

    /**
     * Validates an optional String parameter.
     *
     * <p>This method validates that if the parameter is present:
     * <ul>
     *   <li>The parameter value is a String</li>
     *   <li>The String is not empty</li>
     * </ul>
     * If the parameter is not present, the validation succeeds with a null value.</p>
     *
     * @param <T>       type parameter (unused, possibly for future extension)
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the optional String parameter to validate
     * @return a Result containing the validated String value (or null if not present),
     * or an error if validation fails
     */
    public static <T> Result<String, McpSchema.CallToolResult> OptionalString(
            Map<String, Object> arguments,
            String paramName
    ) {
        return optional(arguments, paramName).andThen(validateString(paramName));
    }

    private static Function<Object, Result<String, McpSchema.CallToolResult>> validateString(String paramName) {
        return x -> {
            if (x == null || x instanceof String) {
                return Result.ok((String) x);
            } else {
                return Result.err(McpSchema.CallToolResult.builder()
                        .addTextContent(
                                paramName + " parameter must be a string"
                        )
                        .isError(true)
                        .build());
            }
        };
    }

    /**
     * Validates a required Long parameter.
     *
     * <p>This method validates that:
     * <ul>
     *   <li>The parameter exists in the arguments map</li>
     *   <li>The parameter value can be converted to a Long</li>
     * </ul>
     * The method accepts Long, Integer, or any Number type and converts it to Long.</p>
     *
     * @param <T>       type parameter (unused, possibly for future extension)
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the Long parameter to validate
     * @return a Result containing the validated Long value, or an error if validation fails
     */
    public static <T> Result<Long, McpSchema.CallToolResult> Long(
            Map<String, Object> arguments,
            String paramName
    ) {
        return required(arguments, paramName).andThen(validateLong(paramName));
    }

    /**
     * Validates a required Boolean parameter.
     *
     * <p>This method validates that:
     * <ul>
     *   <li>The parameter exists in the arguments map</li>
     *   <li>The parameter value is a Boolean</li>
     * </ul>
     * </p>
     *
     * @param <T>       type parameter (unused, possibly for future extension)
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the Boolean parameter to validate
     * @return a Result containing the validated Boolean value, or an error if validation fails
     */
    public static <T> Result<Boolean, McpSchema.CallToolResult> Boolean(
            Map<String, Object> arguments,
            String paramName
    ) {
        return required(arguments, paramName).andThen(validateBoolean(paramName));
    }

    /**
     * Validates an optional Boolean parameter.
     *
     * <p>This method validates that if the parameter is present, it must be a Boolean.
     * If the parameter is not present or is null, the validation succeeds with a null value.</p>
     *
     * @param <T>       type parameter (unused, possibly for future extension)
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the optional Boolean parameter to validate
     * @return a Result containing the validated Boolean value (or null if not present),
     * or an error if validation fails
     */
    public static <T> Result<Boolean, McpSchema.CallToolResult> OptionalBoolean(
            Map<String, Object> arguments,
            String paramName
    ) {
        var o = optional(arguments, paramName);

        if (o.isOk() && o.unwrap() == null) {
            return Result.ok(null);
        }

        return o.andThen(validateBoolean(paramName));
    }

    /**
     * Validates an optional Long parameter.
     *
     * <p>This method validates that if the parameter is present, it can be converted to a Long.
     * If the parameter is not present or is null, the validation succeeds with a null value.
     * The method accepts Long, Integer, or any Number type and converts it to Long.</p>
     *
     * @param <T>       type parameter (unused, possibly for future extension)
     * @param arguments the map containing the parameters to validate
     * @param paramName the name of the optional Long parameter to validate
     * @return a Result containing the validated Long value (or null if not present),
     * or an error if validation fails
     */
    public static <T> Result<Long, McpSchema.CallToolResult> OptionalLong(
            Map<String, Object> arguments,
            String paramName
    ) {
        var o = optional(arguments, paramName);

        if (o.isOk() && o.unwrap() == null) {
            return Result.ok(null);
        }

        return o.andThen(validateLong(paramName));
    }

    private static <T> Function<T, Result<Boolean, McpSchema.CallToolResult>> validateBoolean(String paramName) {
        return x -> {
            if (x == null || x instanceof Boolean) {
                return Result.ok((Boolean) x);
            } else {
                return Result.err(McpSchema.CallToolResult.builder()
                        .addTextContent(
                                paramName + " parameter must be a boolean: true or false"
                        )
                        .isError(true)
                        .build());
            }
        };
    }

    private static <T> Function<T, Result<Long, McpSchema.CallToolResult>> validateLong(String paramName) {
        return x -> switch (x) {
            case null -> Result.ok((Long) x);
            case Long l -> Result.ok((Long) x);
            case Integer i -> Result.ok(i.longValue());
            case Number number -> Result.ok(number.longValue());
            default -> Result.err(McpSchema.CallToolResult.builder()
                    .addTextContent(
                            paramName + " parameter must be a long"
                    )
                    .isError(true)
                    .build());
        };
    }

    /**
     * Retrieves an optional parameter from the arguments map.
     *
     * <p>This method simply retrieves the value associated with the parameter name
     * from the arguments map. It always returns a successful Result, even if the
     * parameter is not present (in which case the value will be null).</p>
     *
     * @param arguments the map containing the parameters
     * @param paramName the name of the parameter to retrieve
     * @return a Result containing the parameter value (which may be null)
     */
    public static Result<Object, McpSchema.CallToolResult> optional(
            Map<String, Object> arguments,
            String paramName
    ) {
        return Result.ok(arguments.get(paramName));
    }

    /**
     * Validates an optional List parameter with a specific element type.
     *
     * <p>This method validates that if the parameter is present:
     * <ul>
     *   <li>The parameter value is a List</li>
     *   <li>All elements in the list are of the specified type (if type checking is needed)</li>
     * </ul>
     * If the parameter is not present or is null, the validation succeeds with a null value.</p>
     *
     * @param <T>         the type of elements expected in the list
     * @param arguments   the map containing the parameters to validate
     * @param paramName   the name of the optional List parameter to validate
     * @param elementType the Class of the expected element type (for type checking)
     * @return a Result containing the validated List value (or null if not present),
     * or an error if validation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<List<T>, McpSchema.CallToolResult> OptionalList(
            Map<String, Object> arguments,
            String paramName,
            Class<T> elementType
    ) {
        var o = optional(arguments, paramName);

        if (o.isOk() && o.unwrap() == null) {
            return Result.ok(null);
        }

        return o.andThen(validateList(paramName, elementType));
    }

    /**
     * Validates a required List parameter with a specific element type.
     *
     * <p>This method validates that:
     * <ul>
     *   <li>The parameter exists in the arguments map</li>
     *   <li>The parameter value is a List</li>
     *   <li>The List is not empty</li>
     *   <li>All elements in the list are of the specified type (if type checking is needed)</li>
     * </ul>
     * </p>
     *
     * @param <T>         the type of elements expected in the list
     * @param arguments   the map containing the parameters to validate
     * @param paramName   the name of the List parameter to validate
     * @param elementType the Class of the expected element type (for type checking)
     * @return a Result containing the validated List value, or an error if validation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<List<T>, McpSchema.CallToolResult> List(
            Map<String, Object> arguments,
            String paramName,
            Class<T> elementType
    ) {
        return required(arguments, paramName).andThen(validateList(paramName, elementType));
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<Object, Result<List<T>, McpSchema.CallToolResult>> validateList(
            String paramName,
            Class<T> elementType
    ) {
        return x -> {
            if (!(x instanceof List)) {
                return Result.err(McpSchema.CallToolResult.builder()
                        .addTextContent(
                                paramName + " parameter must be a list"
                        )
                        .isError(true)
                        .build());
            }

            List<?> list = (List<?>) x;

            // Type check elements if not Object.class
            if (!elementType.equals(Object.class)) {
                for (int i = 0; i < list.size(); i++) {
                    Object element = list.get(i);
                    if (element != null && !elementType.isInstance(element)) {
                        return Result.err(McpSchema.CallToolResult.builder()
                                .addTextContent(
                                        paramName + " parameter list element at index " + i +
                                                " must be of type " + elementType.getSimpleName()
                                )
                                .isError(true)
                                .build());
                    }
                }
            }

            // Safe cast after validation
            return Result.ok((List<T>) list);
        };
    }
}