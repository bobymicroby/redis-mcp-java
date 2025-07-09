package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F6;
import io.redis.mcp.java.core.validation.Result;

/**
 * @author Borislav Ivanov
 */
public class Combine6<A, B, C, D, E, F, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;
    private final Result<E, TError> resultE;
    private final Result<F, TError> resultF;

    public Combine6(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD,
            Result<E, TError> resultE,
            Result<F, TError> resultF
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
        this.resultE = resultE;
        this.resultF = resultF;
    }

    public <G> Result<G, TError> with(F6<A, B, C, D, E, F, G> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.andThen(d ->
                                        resultE.andThen(e ->
                                                resultF.map(f_ -> f.apply(a, b, c, d, e, f_))
                                        )
                                )
                        )
                )
        );
    }
}
