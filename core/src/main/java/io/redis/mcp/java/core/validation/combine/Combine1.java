package io.redis.mcp.java.core.validation.combine;

import io.redis.mcp.java.core.validation.Result;
import java.util.function.Function;

/**
 * @author Borislav Ivanov
 */
public class Combine1<A, TError> {

    private final Result<A, TError> resultA;

    public Combine1(Result<A, TError> resultA) {
        this.resultA = resultA;
    }

    public <B> Result<B, TError> with(Function<A, B> f) {
        return resultA.map(f::apply);
    }
}
