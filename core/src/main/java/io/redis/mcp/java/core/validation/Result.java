package io.redis.mcp.java.core.validation;

import io.redis.mcp.java.core.validation.combine.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * A type-safe error handling mechanism inspired by Rust's Result type and Haskell's Either
 *
 * <p>Result represents either success (Ok) or failure (Err) and provides
 * methods for transforming, combining, and extracting values safely.</p>
 *
 * @param <T>      the type of the success value
 * @param <TError> the type of the error value
 * @author Borislav Ivanov
 */
public sealed interface Result<T, TError> permits Result.Ok, Result.Err {
    /**
     * Represents a successful result containing a value.
     */
    record Ok<T, TError>(T value) implements Result<T, TError> {}

    /**
     * Represents a failed result containing an error.
     */
    record Err<T, TError>(TError value) implements Result<T, TError> {}

    /**
     * Creates a successful Result containing the given value.
     *
     * @param value the success value
     * @return a new Ok result
     */
    static <T, TError> Result<T, TError> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failed Result containing the given error.
     *
     * @param value the error value
     * @return a new Err result
     */
    static <T, TError> Result<T, TError> err(TError value) {
        return new Err<>(value);
    }

    /**
     * Returns true if the result is Ok.
     *
     * @return true if this is an Ok result, false otherwise
     */
    default boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * Returns true if the result is Err.
     *
     * @return true if this is an Err result, false otherwise
     */
    default boolean isErr() {
        return this instanceof Err;
    }

    /**
     * Returns the contained Ok value, throwing if the result is Err.
     *
     * @return the success value
     * @throws NoSuchElementException if the result is Err
     */
    default T unwrap() {
        return switch (this) {
            case Ok<T, TError>(var value) -> value;
            case Err<T, TError>(var error) -> throw new NoSuchElementException(
                "Called unwrap on an Err value: " + error
            );
        };
    }

    /**
     * Returns the contained Err value, throwing if the result is Ok.
     *
     * @return the error value
     * @throws NoSuchElementException if the result is Ok
     */
    default TError unwrapErr() {
        return switch (this) {
            case Err<T, TError>(var value) -> value;
            case Ok<T, TError>(var ok) -> throw new NoSuchElementException(
                "Called unwrapErr on an Ok value: " + ok
            );
        };
    }

    /**
     * Returns the contained Ok value or throws with a custom message.
     *
     * @param message the error message to use if unwrapping fails
     * @return the success value
     * @throws NoSuchElementException if the result is Err
     */
    default T expect(String message) {
        return switch (this) {
            case Ok<T, TError>(var value) -> value;
            case Err<T, TError>(var error) -> throw new NoSuchElementException(
                message + ": " + error
            );
        };
    }

    /**
     * Returns the contained Err value or throws with a custom message.
     *
     * @param message the error message to use if unwrapping fails
     * @return the error value
     * @throws NoSuchElementException if the result is Ok
     */
    default TError expectErr(String message) {
        return switch (this) {
            case Err<T, TError>(var value) -> value;
            case Ok<T, TError>(var ok) -> throw new NoSuchElementException(
                message + ": " + ok
            );
        };
    }

    /**
     * Returns the contained Ok value or a provided default.
     *
     * @param defaultValue the default value to return if the result is Err
     * @return the success value or default
     */
    default T unwrapOr(T defaultValue) {
        return switch (this) {
            case Ok<T, TError>(var value) -> value;
            case Err<T, TError> e -> defaultValue;
        };
    }

    /**
     * Returns the contained Ok value or computes it from the error.
     *
     * @param op function to compute a default value from the error
     * @return the success value or computed default
     */
    default T unwrapOrElse(Function<TError, T> op) {
        return switch (this) {
            case Ok<T, TError>(var value) -> value;
            case Err<T, TError>(var error) -> op.apply(error);
        };
    }

    /**
     * Converts the Result into an Optional of the success value.
     *
     * @return Optional containing the value if Ok, empty if Err
     */
    default Optional<T> ok() {
        return switch (this) {
            case Ok<T, TError>(var value) -> Optional.of(value);
            case Err<T, TError> e -> Optional.empty();
        };
    }

    /**
     * Converts the Result into an Optional of the error value.
     *
     * @return Optional containing the error if Err, empty if Ok
     */
    default Optional<TError> err() {
        return switch (this) {
            case Err<T, TError>(var value) -> Optional.of(value);
            case Ok<T, TError> ok -> Optional.empty();
        };
    }

    /**
     * Maps a Result<T, TError> to Result<U, TError> by applying a function to the Ok value.
     *
     * @param op the mapping function
     * @return a new Result with the mapped value or the original error
     */
    default <U> Result<U, TError> map(Function<T, U> op) {
        return switch (this) {
            case Ok<T, TError>(var value) -> new Ok<>(op.apply(value));
            case Err<T, TError>(var error) -> new Err<>(error);
        };
    }

    /**
     * Maps a Result<T, TError> to Result<T, F> by applying a function to the Err value.
     *
     * @param op the error mapping function
     * @return a new Result with the original value or mapped error
     */
    default <F> Result<T, F> mapErr(Function<TError, F> op) {
        return switch (this) {
            case Err<T, TError>(var error) -> new Err<>(op.apply(error));
            case Ok<T, TError>(var value) -> new Ok<>(value);
        };
    }

    /**
     * Returns the provided default if Err, or applies the function to the Ok value.
     *
     * @param defaultValue the default value for Err case
     * @param f            the function to apply to Ok value
     * @return the mapped value or default
     */
    default <U> U mapOr(U defaultValue, Function<T, U> f) {
        return switch (this) {
            case Ok<T, TError>(var value) -> f.apply(value);
            case Err<T, TError> e -> defaultValue;
        };
    }

    /**
     * Applies a function to the Ok value, or a different function to the Err value.
     *
     * @param defaultFunc function to apply to error
     * @param f           function to apply to success value
     * @return the result of applying the appropriate function
     */
    default <U> U mapOrElse(Function<TError, U> defaultFunc, Function<T, U> f) {
        return switch (this) {
            case Ok<T, TError>(var value) -> f.apply(value);
            case Err<T, TError>(var error) -> defaultFunc.apply(error);
        };
    }

    /**
     * Returns res if the result is Ok, otherwise returns the Err value.
     *
     * @param res the result to return if this is Ok
     * @return res if Ok, otherwise this Err
     */
    default <U> Result<U, TError> and(Result<U, TError> res) {
        return switch (this) {
            case Ok<T, TError> ok -> res;
            case Err<T, TError>(var error) -> new Err<>(error);
        };
    }

    /**
     * Chains another Result-producing operation if this is Ok.
     *
     * @param op function that produces a new Result
     * @return the result of the operation or the original error
     */
    default <U> Result<U, TError> andThen(Function<T, Result<U, TError>> op) {
        return switch (this) {
            case Ok<T, TError>(var value) -> op.apply(value);
            case Err<T, TError>(var error) -> new Err<>(error);
        };
    }

    /**
     * Returns this if Ok, otherwise returns res.
     *
     * @param res the result to return if this is Err
     * @return this if Ok, otherwise res
     */
    default <F> Result<T, F> or(Result<T, F> res) {
        return switch (this) {
            case Ok<T, TError>(var value) -> new Ok<>(value);
            case Err<T, TError> e -> res;
        };
    }

    /**
     * Returns this if Ok, otherwise calls op with the error value.
     *
     * @param op function to call with error value
     * @return this if Ok, otherwise the result of op
     */
    default <F> Result<T, F> orElse(Function<TError, Result<T, F>> op) {
        return switch (this) {
            case Ok<T, TError>(var value) -> new Ok<>(value);
            case Err<T, TError>(var error) -> op.apply(error);
        };
    }

    /**
     * Executes a function with the Ok value if present.
     *
     * @param f the function to execute
     */
    default void ifOk(Function<T, Void> f) {
        if (this instanceof Ok<T, TError>(var value)) {
            f.apply(value);
        }
    }

    /**
     * Executes a function with the Err value if present.
     *
     * @param f the function to execute
     */
    default void ifErr(Function<TError, Void> f) {
        if (this instanceof Err<T, TError>(var error)) {
            f.apply(error);
        }
    }

    /**
     * Pattern matches on the Result, applying the appropriate function.
     *
     * @param okHandler  function to apply if Ok
     * @param errHandler function to apply if Err
     * @return the result of the applied function
     */
    default <R> R match(
        Function<T, R> okHandler,
        Function<TError, R> errHandler
    ) {
        return switch (this) {
            case Ok<T, TError>(var value) -> okHandler.apply(value);
            case Err<T, TError>(var error) -> errHandler.apply(error);
        };
    }

    /**
     * Collects an array of Results into a single Result containing a list.
     *
     * @param results the results to collect
     * @return Ok with all values if all are Ok, otherwise the first Err
     */
    @SafeVarargs
    static <T, TError> Result<List<T>, TError> collect(
        Result<T, TError>... results
    ) {
        var collected = new ArrayList<T>();
        for (var result : results) {
            switch (result) {
                case Ok<T, TError>(var value) -> collected.add(value);
                case Err<T, TError>(var error) -> {
                    return new Err<>(error);
                }
            }
        }
        return new Ok<>(collected);
    }

    /**
     * Unwraps the Ok value or throws an exception (similar to Rust's ? operator).
     *
     * @return the success value
     * @throws Exception if the result is Err
     */
    default T unwrapOrThrow() throws Exception {
        return switch (this) {
            case Ok<T, TError>(var value) -> value;
            case Err<T, TError>(var error) -> throw new Exception(
                error.toString()
            );
        };
    }

    /**
     * Calls a function with the Ok value for side effects, returning self.
     *
     * @param f the function to call
     * @return this Result unchanged
     */
    default Result<T, TError> inspect(Function<T, Void> f) {
        if (this instanceof Ok<T, TError>(var value)) {
            f.apply(value);
        }
        return this;
    }

    /**
     * Calls a function with the Err value for side effects, returning self.
     *
     * @param f the function to call
     * @return this Result unchanged
     */
    default Result<T, TError> inspectErr(Function<TError, Void> f) {
        if (this instanceof Err<T, TError>(var error)) {
            f.apply(error);
        }
        return this;
    }

    /**
     * Combines 1 Result into a builder for mapping operations.
     *
     * @param resultA the result to combine
     * @return a Combine1 builder
     */
    static <TError, A> Combine1<A, TError> combine(Result<A, TError> resultA) {
        return new Combine1<>(resultA);
    }

    /**
     * Combines 2 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @return a Combine2 builder
     */
    static <TError, A, B> Combine2<A, B, TError> combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB
    ) {
        return new Combine2<>(resultA, resultB);
    }

    /**
     * Combines 3 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @return a Combine3 builder
     */
    static <TError, A, B, C> Combine3<A, B, C, TError> combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC
    ) {
        return new Combine3<>(resultA, resultB, resultC);
    }

    /**
     * Combines 4 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @return a Combine4 builder
     */
    static <TError, A, B, C, D> Combine4<A, B, C, D, TError> combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD
    ) {
        return new Combine4<>(resultA, resultB, resultC, resultD);
    }

    /**
     * Combines 5 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @return a Combine5 builder
     */
    static <TError, A, B, C, D, E> Combine5<A, B, C, D, E, TError> combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE
    ) {
        return new Combine5<>(resultA, resultB, resultC, resultD, resultE);
    }

    /**
     * Combines 6 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @return a Combine6 builder
     */
    static <TError, A, B, C, D, E, F> Combine6<
        A,
        B,
        C,
        D,
        E,
        F,
        TError
    > combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE,
        Result<F, TError> resultF
    ) {
        return new Combine6<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF
        );
    }

    /**
     * Combines 7 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @return a Combine7 builder
     */
    static <TError, A, B, C, D, E, F, G> Combine7<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        TError
    > combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE,
        Result<F, TError> resultF,
        Result<G, TError> resultG
    ) {
        return new Combine7<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG
        );
    }

    /**
     * Combines 8 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @return a Combine8 builder
     */
    static <TError, A, B, C, D, E, F, G, H> Combine8<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        TError
    > combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE,
        Result<F, TError> resultF,
        Result<G, TError> resultG,
        Result<H, TError> resultH
    ) {
        return new Combine8<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH
        );
    }

    /**
     * Combines 9 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @return a Combine9 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I> Combine9<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        TError
    > combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE,
        Result<F, TError> resultF,
        Result<G, TError> resultG,
        Result<H, TError> resultH,
        Result<I, TError> resultI
    ) {
        return new Combine9<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI
        );
    }

    /**
     * Combines 10 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @return a Combine10 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J> Combine10<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        TError
    > combine(
        Result<A, TError> resultA,
        Result<B, TError> resultB,
        Result<C, TError> resultC,
        Result<D, TError> resultD,
        Result<E, TError> resultE,
        Result<F, TError> resultF,
        Result<G, TError> resultG,
        Result<H, TError> resultH,
        Result<I, TError> resultI,
        Result<J, TError> resultJ
    ) {
        return new Combine10<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ
        );
    }

    /**
     * Combines 11 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @param resultK the eleventh result
     * @return a Combine11 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J, K> Combine11<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        TError
    > combine(
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
        Result<K, TError> resultK
    ) {
        return new Combine11<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ,
            resultK
        );
    }

    /**
     * Combines 12 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @param resultK the eleventh result
     * @param resultL the twelfth result
     * @return a Combine12 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J, K, L> Combine12<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        TError
    > combine(
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
        Result<L, TError> resultL
    ) {
        return new Combine12<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ,
            resultK,
            resultL
        );
    }

    /**
     * Combines 13 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @param resultK the eleventh result
     * @param resultL the twelfth result
     * @param resultM the thirteenth result
     * @return a Combine13 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J, K, L, M> Combine13<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        M,
        TError
    > combine(
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
        Result<M, TError> resultM
    ) {
        return new Combine13<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ,
            resultK,
            resultL,
            resultM
        );
    }

    /**
     * Combines 14 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @param resultK the eleventh result
     * @param resultL the twelfth result
     * @param resultM the thirteenth result
     * @param resultN the fourteenth result
     * @return a Combine14 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J, K, L, M, N> Combine14<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        M,
        N,
        TError
    > combine(
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
        Result<N, TError> resultN
    ) {
        return new Combine14<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ,
            resultK,
            resultL,
            resultM,
            resultN
        );
    }

    /**
     * Combines 15 Results into a builder for mapping operations.
     *
     * @param resultA the first result
     * @param resultB the second result
     * @param resultC the third result
     * @param resultD the fourth result
     * @param resultE the fifth result
     * @param resultF the sixth result
     * @param resultG the seventh result
     * @param resultH the eighth result
     * @param resultI the ninth result
     * @param resultJ the tenth result
     * @param resultK the eleventh result
     * @param resultL the twelfth result
     * @param resultM the thirteenth result
     * @param resultN the fourteenth result
     * @param resultO the fifteenth result
     * @return a Combine15 builder
     */
    static <TError, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> Combine15<
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        H,
        I,
        J,
        K,
        L,
        M,
        N,
        O,
        TError
    > combine(
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
        return new Combine15<>(
            resultA,
            resultB,
            resultC,
            resultD,
            resultE,
            resultF,
            resultG,
            resultH,
            resultI,
            resultJ,
            resultK,
            resultL,
            resultM,
            resultN,
            resultO
        );
    }
}
