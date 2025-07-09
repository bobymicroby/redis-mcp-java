package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes six arguments and returns a result.
 
 * @author Borislav Ivanov
 */
@FunctionalInterface
public interface F6<A, B, C, D, E, F, G> {
   
    G apply(A a, B b, C c, D d, E e, F f);
}
