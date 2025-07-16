package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes nine arguments and returns a result.
 *
 
 * 
 */
@FunctionalInterface
public interface F9<A, B, C, D, E, F, G, H, I, J> {
   
    J apply(A a, B b, C c, D d, E e, F f, G g, H h, I i);
}
