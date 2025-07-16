package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes eleven arguments and returns a result.
 
 * 
 */
@FunctionalInterface
public interface F11<A, B, C, D, E, F, G, H, I, J, K, L> {
   
    L apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k);
}

