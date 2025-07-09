package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes five arguments and returns a result.
 *
 
 * @author Borislav Ivanov
 */
@FunctionalInterface
public interface F5<A, B, C, D, E, F> {
   
    F apply(A a, B b, C c, D d, E e);
}
