package io.redis.mcp.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.redis.mcp.java.config.RedisProperties;
import io.redis.mcp.java.core.tooling.RedisToolsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.List;

/**
 * 
 */
@SpringBootApplication
@EnableConfigurationProperties(RedisProperties.class)
public class JavaApplication {

    private static final Logger logger = LoggerFactory.getLogger(JavaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JavaApplication.class, args);
    }

    @Bean
    public WebFluxSseServerTransportProvider sseServerTransport() {
        WebFluxSseServerTransportProvider.Builder builder = WebFluxSseServerTransportProvider.builder();
        return builder.objectMapper(new ObjectMapper()).messageEndpoint("/mcp/message").build();
    }


    @Bean
    public RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransportProvider provider) {
        return provider.getRouterFunction();
    }

    @Bean
    public McpAsyncServer mcpServer(McpServerTransportProvider transportProvider, RedisProperties redisProperties) {

        logger.info("Starting MCP server...");
        logger.info("the transport provider is: {}", transportProvider.getClass().getName());
        logger.info("Redis URL: {}", redisProperties.url());
        logger.info("Redis pool size: {}", redisProperties.pool().size());

        var tools =
                RedisToolsRepository.getAsyncToolSpecifications(redisProperties.url(), redisProperties.pool().size());

        var capabilities = McpSchema.ServerCapabilities.builder().tools(true).logging().build();

        var server = McpServer.async(transportProvider).serverInfo("redis-mcp-java", "1.0.0")
                .capabilities(capabilities).tools(tools)
                .build();

        tools.forEach(tool -> {
            logger.info("Registered tool: {}", tool.tool().name());
        });

        return server;
    }
}
