package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F5;
import io.redis.mcp.java.core.validation.Result;

/**
 * @author Borislav Ivanov
 */
public class Combine5<A, B, C, D, E, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;
    private final Result<E, TError> resultE;

    public Combine5(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD,
            Result<E, TError> resultE
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
        this.resultE = resultE;
    }

    public <F> Result<F, TError> with(F5<A, B, C, D, E, F> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.andThen(d ->
                                        resultE.map(e -> f.apply(a, b, c, d, e))
                                )
                        )
                )
        );
    }
}
