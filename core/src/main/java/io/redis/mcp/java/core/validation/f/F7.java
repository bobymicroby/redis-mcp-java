package io.redis.mcp.java.core.validation.f;

/**
 * A function that takes seven arguments and returns a result.
 *
 * 
 */
@FunctionalInterface
public interface F7<A, B, C, D, E, F, G, H> {
    /**
     * Applies this function to the given arguments.
     *
     * @param a the first function argument
     * @param b the second function argument
     * @param c the third function argument
     * @param d the fourth function argument
     * @param e the fifth function argument
     * @param f the sixth function argument
     * @param g the seventh function argument
     * @return the function result
     */
    H apply(A a, B b, C c, D d, E e, F f, G g);
}
