> ⚠️ **Beta Software**: This project is under active development. Expect bugs and breaking changes.


> For the official Redis MCP server, please visit [redis-mcp](https://github.com/redis/mcp-redis)
> This project is intended to be used as library to develop custom MCP tools for Redis ( e.g. in-house Redis modules,
> custom Lua scripts or Redis Functions )
> The existing tools are provided as examples and can be used as a reference for developing your own tools.

# Redis MCP Java

A Java implementation of Model Context Protocol (MCP) tools for Redis operations, providing both Lettuce and Jedis
client support with automatic tool discovery and schema generation.

## Architecture Overview

The Redis MCP Java library follows a layered architecture that separates concerns between connection
management,validation, MCP tool handling, and MCP protocol integration.

### Core Components

```
redis-mcp-java/
├── core/                           # Core MCP Redis functionality
│   └── src/main/java/io/redis/mcp/java/core/
│       ├── handlers/               # Redis command implementations
│       │   ├── lettuce/           # Lettuce-based handlers
│       │   └── jedis/             # Jedis-based handlers
│       ├── net/                   # Connection management
│       ├── tooling/               # Tool discovery and MCP integration
│       └── validation/            # Input validation and error handling
└── spring/                        # Spring Boot integration (optional)
```

### Key Architectural Patterns

#### 1. Handler-Based Tool Implementation

Each Redis command is implemented as a separate handler class that extends either `LettuceHandler` or `JedisHandler`:

```java
public class GetHandler extends LettuceHandler {
    public record GetRequest(
            @Nonnull @Description("The Redis key to retrieve") String key
    ) {
    }

    @Override
    public McpSchema.Tool toolSchema() {
        return new McpSchema.Tool(
                "redis_get",
                "Retrieve a value from Redis by key",
                recordToJSONSchema(GetRequest.class)
        );
    }

    // Implementation methods...
}
```

#### 2. Automatic Tool Discovery

The `HandlerScanner` uses reflection to automatically discover and instantiate all handler classes:

- Scans specified packages for `LettuceHandler` and `JedisHandler` subclasses
- Instantiates handlers with Redis connection providers
- Validates tool name uniqueness across all handlers
- Builds tool registry for MCP server registration

#### 3. Connection Management

The `Redis` interface abstracts connection management with support for:

- **Lazy Connection Pooling**: Connections created on-demand
- **Round-Robin Distribution**: Load balancing across pooled connections
- **Dual Client Support**: Both Lettuce (async/reactive) and Jedis (sync) clients
- **Connection Caching**: Efficient connection reuse

#### 4. Schema Generation

Automatic MCP JSON Schema generation from Java records:

- `@Description` annotations provide field documentation
- `@Nonnull` annotations mark required fields

#### 5. Validation

Type-safe user input validation

- **Result<T, E>**: Represents success or failure without exceptions
- **Validation Combinators**: Compose multiple validations
- **Type Safety**: Compile-time validation of parameter types
- **Error Accumulation**: Collect multiple validation errors

## How to Add New MCP Tools

Adding new Redis tools involves creating handler classes that integrate automatically with the MCP framework.

### Step 1: Choose Your Client

Decide whether to use Lettuce or Jedis :

```java

public class MyHandler extends LettuceHandler {
    // Implementation
}

public class MyHandler extends JedisHandler {
    // Implementation
}
```

### Step 2: Define Request Schema

Create a Java record with validation annotations:

```java
public record MyRequest(
        @Nonnull
        @Description("The Redis key to operate on")
        String key,

        @Description("Optional timeout in seconds")
        Long timeout,

        @Description("Operation mode")
        String mode
) {
}
```

### Step 3: Implement Tool Schema

Define the MCP tool specification:

```java

@Override
public McpSchema.Tool toolSchema() {
    return new McpSchema.Tool(
            "redis_my_command",                    // Tool name (must be unique)
            "Description of what this tool does",  // Tool description
            recordToJSONSchema(MyRequest.class)    // Auto-generated schema
    );
}
```

### Step 4: Add Input Validation

Use the validation framework to safely parse arguments:

```java
private static Result<MyRequest, McpSchema.CallToolResult> validateRequest(
        Map<String, Object> arguments
) {
    return Result.combine(
            V.String(arguments, "key"),           // Required string
            V.OptionalLong(arguments, "timeout"), // Optional long
            V.OptionalString(arguments, "mode")   // Optional string
    ).with(MyRequest::new);
}
```

### Step 5: Implement Sync Handler

```java

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

    var jedis = getConnection();

    var request = result.unwrap();
    var value = jedis.myCommand(request.key);


    return McpSchema.CallToolResult.builder()
            .addTextContent("Result: " + value)
            .isError(false)
            .build();
}
```

### Step 6: Implement Async Handler

```java

@Override
public CompletableFuture<McpSchema.CallToolResult> handleAsync(
        McpAsyncServerExchange exchange,
        Map<String, Object> arguments
) {
    var result = validateRequest(arguments);

    if (result.isErr()) {
        return CompletableFuture.completedFuture(result.unwrapErr());
    }

    var request = result.unwrap();

    return getConnectionAsync()
            .thenCompose(conn -> conn.async().myCommand(request.key))
            .thenApply(value -> McpSchema.CallToolResult.builder()
                    .addTextContent("Result: " + value)
                    .isError(false)
                    .build());


});
        }
```

### Step 7: Package Placement

#### Contributing to This Project

If you are contributing a handler to this project, place your handler in the appropriate package:

- **Lettuce handlers**: `io.redis.mcp.java.core.handlers.lettuce`
- **Jedis handlers**: `io.redis.mcp.java.core.handlers.jedis`

Handlers in these packages will be automatically discovered and registered when users call:

```java
RedisToolsRepository.getSyncToolSpecifications(String redisUrl, int maxConnections)
```

#### Custom Handler Development

If you are developing your own handler in a custom package, pass the package name to the `RedisToolsRepository` for
manual instantiation:

```java
RedisToolsRepository.getSyncToolSpecifications(String redisUrl, int maxConnections, List<String> packages)
```

## Usage

### Basic Setup

```java
// Create tool specifications
var toolSpecs = RedisToolsRepository.getSyncToolSpecifications(
                "redis://localhost:6379",
                10  // max connections
        );

// Register with the Java MCP server
mcpServer.

registerTools(toolSpecs);
```

### Custom Package Scanning

```java
var customPackages = List.of(
        "com.mycompany.redis.handlers",
        "io.redis.mcp.java.core.handlers.lettuce"
);

var toolSpecs = RedisToolsRepository.getSyncToolSpecifications(
        "redis://localhost:6379",
        10,
        customPackages
);
```

## Spring Boot MCP Server

The project includes a ready-to-run Spring Boot application that provides a complete MCP server with Redis tool
integration.

> **Future Development**: We are planning to develop Spring-specific annotations, annotation processors, and
> auto-configuration starters to streamline the integration of redis-mcp-java with Spring MCP in a future release. This
> will provide declarative configuration and automatic setup. For now, manual MCP server configuration is required, but
> the current approach is straightforward and provides full control over the setup.

### Configuration

Configure Redis connection settings in `spring/src/main/resources/application.properties`:

```properties
# Redis MCP Configuration
redis.mcp.url=redis://localhost:6379
redis.mcp.pool.size=4
```

### Running the Application

#### Using Gradle

```bash
# Run the Spring Boot application
./gradlew :spring:bootRun

# Or build and run the JAR
./gradlew :spring:bootJar
java -jar spring/build/libs/spring-*.jar
```

#### Configuration Override

You can override configuration via environment variables or command-line arguments:

```bash
# Via environment variables
export REDIS_MCP_URL=redis://production-redis:6379
export REDIS_MCP_POOL_SIZE=10
./gradlew :spring:bootRun

# Via command line arguments
./gradlew :spring:bootRun --args="--redis.mcp.url=redis://staging:6379 --redis.mcp.pool.size=8"

# Or with JAR
java -jar spring/build/libs/spring-*.jar --redis.mcp.url=redis://custom:6379
```

### MCP Server Endpoints

Once running, the MCP server provides:

- **Server-Sent Events Endpoint**: `http://localhost:8080/mcp/message`
- **Available Tools**: Automatically discovered Redis handlers (GET, SET, JSON.GET, JSON.SET)

