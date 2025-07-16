package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes twelve arguments and returns a result.
 *
 * 
 */
@FunctionalInterface
public interface F12<A, B, C, D, E, F, G, H, I, J, K, L, M> {

    M apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l);
}
