package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.Result;

import java.util.function.BiFunction;

/**
 * @author Borislav Ivanov
 */
public class Combine2<A, B, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;

    public Combine2(
            Result<A, TError> resultA,
            Result<B, TError> resultB
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
    }

    public <C> Result<C, TError> with(BiFunction<A, B, C> f) {
        return resultA.andThen(a ->
                resultB.map(b -> f.apply(a, b))
        );
    }
}
