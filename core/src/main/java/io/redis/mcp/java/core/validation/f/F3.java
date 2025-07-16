package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes three arguments and returns a result.
 *
 * 
 */
@FunctionalInterface
public interface F3<A, B, C, D> {
   
    D apply(A a, B b, C c);
}
