package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes eight arguments and returns a result.
 *
 * 
 */
@FunctionalInterface
public interface F8<A, B, C, D, E, F, G, H, I> {
   
    I apply(A a, B b, C c, D d, E e, F f, G g, H h);
}
