package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F4;
import io.redis.mcp.java.core.validation.Result;

/**
 * @author Borislav Ivanov
 */
public class Combine4<A, B, C, D, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;

    public Combine4(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
    }

    public <E> Result<E, TError> with(F4<A, B, C, D, E> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.map(d -> f.apply(a, b, c, d))
                        )
                )
        );
    }
}
