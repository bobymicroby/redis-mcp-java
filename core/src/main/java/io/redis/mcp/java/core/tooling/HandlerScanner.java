package io.redis.mcp.java.core.tooling;


import io.redis.mcp.java.core.net.Redis;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.*;

/**
 * Utility class for scanning and instantiating Redis MCP handlers.
 * <p>
 * This class uses reflection to discover all {@link LettuceHandler} implementations
 * within a specified package and automatically instantiate them with a Redis connection.
 * It's primarily used by the MCP server to build the available tool set at startup.
 * </p>
 * <p>
 * The scanner ensures that:
 * - All handler classes have a constructor accepting a {@link Redis} parameter
 * - Tool names are unique across all discovered handlers
 * - Instantiation failures are properly reported with detailed error messages
 * </p>
 * <p>
 * This approach enables a plugin-like architecture where new Redis commands can be
 * added by simply creating new handler classes in the scanned package.
 * </p>
 *
 * @author Borislav Ivanov

 * @see LettuceHandler
 * @see RedisToolsRepository
 */
public class HandlerScanner {

    /**
     * Scans for and instantiates all LettuceHandler implementations in the specified package.
     * <p>
     * This method uses reflection to find all classes that extend {@link LettuceHandler}
     * within the given package, then instantiates each one using their Redis constructor.
     * The resulting handlers are indexed by their tool name for efficient lookup.
     * </p>
     * <p>
     * Validation performed:
     * - Each handler class must have a constructor accepting {@link Redis}
     * - Tool names must be unique across all handlers
     * - All instantiation must succeed or the entire operation fails
     * </p>
     *
     * @param redis       the Redis connection provider to inject into handlers
     * @param packageName the package name to scan for handler implementations
     * @return a map of tool names to their corresponding handler instances
     * @throws RuntimeException if duplicate tool names are found or instantiation fails
     * @throws IllegalArgumentException if redis or packageName is null
     */
    public static Map<String, RedisHandler> instantiateHandlers(Redis redis, List<String> packages) {
        Map<String, RedisHandler> handlers = new HashMap<>();

        for (String packageName : packages) {
            Reflections reflections = new Reflections(packageName, Scanners.SubTypes);

            // Process both Lettuce and Jedis handlers from this package
            processHandlers(reflections.getSubTypesOf(LettuceHandler.class), redis, handlers, packageName);
            processHandlers(reflections.getSubTypesOf(JedisHandler.class), redis, handlers, packageName);
        }

        return handlers;
    }

    private static <T extends RedisHandler> void processHandlers(
            Set<Class<? extends T>> handlerClasses,
            Redis redis,
            Map<String, RedisHandler> handlers,
            String currentPackage) {

        for (Class<? extends T> handlerClass : handlerClasses) {
            try {
                T instance = handlerClass.getDeclaredConstructor(Redis.class).newInstance(redis);

                String name = instance.toolSchema().name();
                if (handlers.containsKey(name)) {
                    throw new RuntimeException("Duplicate handler found for tool: " + name +
                            " (existing: " + handlers.get(name).getClass().getName() +
                            " from package: " + handlers.get(name).getClass().getPackage().getName() +
                            ", new: " + handlerClass.getName() +
                            " from package: " + currentPackage + ")");
                }

                handlers.put(name, instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + handlerClass.getName() +
                        " from package: " + currentPackage, e);
            }
        }
    }

}