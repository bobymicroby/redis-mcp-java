package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F7;
import io.redis.mcp.java.core.validation.Result;

/**
 * 
 */
public class Combine7<A, B, C, D, E, F, G, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;
    private final Result<E, TError> resultE;
    private final Result<F, TError> resultF;
    private final Result<G, TError> resultG;

    public Combine7(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD,
            Result<E, TError> resultE,
            Result<F, TError> resultF,
            Result<G, TError> resultG
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
        this.resultE = resultE;
        this.resultF = resultF;
        this.resultG = resultG;
    }

    public <H> Result<H, TError> with(F7<A, B, C, D, E, F, G, H> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.andThen(d ->
                                        resultE.andThen(e ->
                                                resultF.andThen(f_ ->
                                                        resultG.map(g -> f.apply(a, b, c, d, e, f_, g))
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
