package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F8;
import io.redis.mcp.java.core.validation.Result;

/**
 * 
 */
public class Combine8<A, B, C, D, E, F, G, H, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;
    private final Result<E, TError> resultE;
    private final Result<F, TError> resultF;
    private final Result<G, TError> resultG;
    private final Result<H, TError> resultH;

    public Combine8(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD,
            Result<E, TError> resultE,
            Result<F, TError> resultF,
            Result<G, TError> resultG,
            Result<H, TError> resultH
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
        this.resultE = resultE;
        this.resultF = resultF;
        this.resultG = resultG;
        this.resultH = resultH;
    }

    public <I> Result<I, TError> with(F8<A, B, C, D, E, F, G, H, I> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.andThen(d ->
                                        resultE.andThen(e ->
                                                resultF.andThen(f_ ->
                                                        resultG.andThen(g ->
                                                                resultH.map(h -> f.apply(a, b, c, d, e, f_, g, h))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
