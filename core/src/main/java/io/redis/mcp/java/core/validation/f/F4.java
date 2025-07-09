package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes four arguments and returns a result.
 *
 
 * @author Borislav Ivanov
 */
@FunctionalInterface
public interface F4<A, B, C, D, E> {
   
    E apply(A a, B b, C c, D d);
}
