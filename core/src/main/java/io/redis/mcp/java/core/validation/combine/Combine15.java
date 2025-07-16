package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.f.F15;
import io.redis.mcp.java.core.validation.Result;

/**
 * 
 */
public class Combine15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, TError> {

    private final Result<A, TError> resultA;
    private final Result<B, TError> resultB;
    private final Result<C, TError> resultC;
    private final Result<D, TError> resultD;
    private final Result<E, TError> resultE;
    private final Result<F, TError> resultF;
    private final Result<G, TError> resultG;
    private final Result<H, TError> resultH;
    private final Result<I, TError> resultI;
    private final Result<J, TError> resultJ;
    private final Result<K, TError> resultK;
    private final Result<L, TError> resultL;
    private final Result<M, TError> resultM;
    private final Result<N, TError> resultN;
    private final Result<O, TError> resultO;

    public Combine15(
            Result<A, TError> resultA,
            Result<B, TError> resultB,
            Result<C, TError> resultC,
            Result<D, TError> resultD,
            Result<E, TError> resultE,
            Result<F, TError> resultF,
            Result<G, TError> resultG,
            Result<H, TError> resultH,
            Result<I, TError> resultI,
            Result<J, TError> resultJ,
            Result<K, TError> resultK,
            Result<L, TError> resultL,
            Result<M, TError> resultM,
            Result<N, TError> resultN,
            Result<O, TError> resultO
    ) {
        this.resultA = resultA;
        this.resultB = resultB;
        this.resultC = resultC;
        this.resultD = resultD;
        this.resultE = resultE;
        this.resultF = resultF;
        this.resultG = resultG;
        this.resultH = resultH;
        this.resultI = resultI;
        this.resultJ = resultJ;
        this.resultK = resultK;
        this.resultL = resultL;
        this.resultM = resultM;
        this.resultN = resultN;
        this.resultO = resultO;
    }

    public <P> Result<P, TError> with(F15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> f) {
        return resultA.andThen(a ->
                resultB.andThen(b ->
                        resultC.andThen(c ->
                                resultD.andThen(d ->
                                        resultE.andThen(e ->
                                                resultF.andThen(f_ ->
                                                        resultG.andThen(g ->
                                                                resultH.andThen(h ->
                                                                        resultI.andThen(i ->
                                                                                resultJ.andThen(j ->
                                                                                        resultK.andThen(k ->
                                                                                                resultL.andThen(l ->
                                                                                                        resultM.andThen(m ->
                                                                                                                resultN.andThen(n ->
                                                                                                                        resultO.map(o -> f.apply(a, b, c, d, e, f_, g, h, i, j, k, l, m, n, o))
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}