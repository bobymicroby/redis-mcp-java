package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F3;
import io.redis.mcp.java.core.validation.Result;

/**
 * 
 */
public class Combine3<A, B, C, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;

    public Combine3(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
    }

    public <D> Result<D, TError> with(F3<A, B, C, D> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.map(c -> f.apply(a, b, c))
                )
        );
    }
}
