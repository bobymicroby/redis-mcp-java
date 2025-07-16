package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes thirteen arguments and returns a result.
 * 
 */
@FunctionalInterface
public interface F13<A, B, C, D, E, F, G, H, I, J, K, L, M, N> {
  
    N apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l, M m);
}
