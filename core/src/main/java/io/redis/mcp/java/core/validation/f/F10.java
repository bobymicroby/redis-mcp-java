package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes ten arguments and returns a result.

 * @author Borislav Ivanov
 */
@FunctionalInterface
public interface F10<A, B, C, D, E, F, G, H, I, J, K> {
  
    K apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);
}