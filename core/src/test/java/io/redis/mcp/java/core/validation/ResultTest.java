package io.redis.mcp.java.core.validation;

import io.redis.mcp.java.core.validation.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 
 */
class ResultTest {

    @Test
    @DisplayName("Factory methods should create correct Result types")
    void testFactoryMethods() {
        var okResult = Result.ok("success");
        var errResult = Result.err("error");

        assertTrue(okResult.isOk());
        assertFalse(okResult.isErr());
        assertFalse(errResult.isOk());
        assertTrue(errResult.isErr());
    }

    @Test
    @DisplayName("isOk and isErr should correctly identify result types")
    void testTypeChecking() {
        var okResult = Result.ok("success");
        var errResult = Result.err("error");

        assertTrue(okResult.isOk());
        assertFalse(okResult.isErr());
        assertFalse(errResult.isOk());
        assertTrue(errResult.isErr());
    }

    @Test
    @DisplayName("unwrap should return value for Ok and throw for Err")
    void testUnwrap() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("success", okResult.unwrap());

        var exception = assertThrows(NoSuchElementException.class, errResult::unwrap);
        assertTrue(exception.getMessage().contains("error"));
    }

    @Test
    @DisplayName("unwrapErr should return error for Err and throw for Ok")
    void testUnwrapErr() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("error", errResult.unwrapErr());

        var exception = assertThrows(NoSuchElementException.class, okResult::unwrapErr);
        assertTrue(exception.getMessage().contains("success"));
    }

    @Test
    @DisplayName("expect should return value for Ok and throw with custom message for Err")
    void testExpect() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("success", okResult.expect("Should not fail"));

        var exception = assertThrows(NoSuchElementException.class,
                () -> errResult.expect("Custom error message"));
        assertTrue(exception.getMessage().contains("Custom error message"));
        assertTrue(exception.getMessage().contains("error"));
    }

    @Test
    @DisplayName("expectErr should return error for Err and throw with custom message for Ok")
    void testExpectErr() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("error", errResult.expectErr("Should not fail"));

        var exception = assertThrows(NoSuchElementException.class,
                () -> okResult.expectErr("Custom error message"));
        assertTrue(exception.getMessage().contains("Custom error message"));
        assertTrue(exception.getMessage().contains("success"));
    }

    @Test
    @DisplayName("unwrapOr should return value for Ok or default for Err")
    void testUnwrapOr() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("success", okResult.unwrapOr("default"));
        assertEquals("default", errResult.unwrapOr("default"));
    }

    @Test
    @DisplayName("unwrapOrElse should return value for Ok or computed value for Err")
    void testUnwrapOrElse() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        Function<String, String> computeDefault = err -> "computed_" + err;

        assertEquals("success", okResult.unwrapOrElse(computeDefault));
        assertEquals("computed_error", errResult.unwrapOrElse(computeDefault));
    }

    @Test
    @DisplayName("ok should convert to Optional correctly")
    void testOkConversion() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals(Optional.of("success"), okResult.ok());
        assertEquals(Optional.empty(), errResult.ok());
    }

    @Test
    @DisplayName("err should convert to Optional correctly")
    void testErrConversion() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals(Optional.empty(), okResult.err());
        assertEquals(Optional.of("error"), errResult.err());
    }

    @Test
    @DisplayName("map should transform Ok value and preserve Err")
    void testMap() {
        var okResult = Result.<Integer, String>ok(5);
        var errResult = Result.<Integer, String>err("error");

        Function<Integer, String> mapper = i -> "value_" + i;

        var mappedOk = okResult.map(mapper);
        var mappedErr = errResult.map(mapper);

        assertTrue(mappedOk.isOk());
        assertEquals("value_5", mappedOk.unwrap());
        assertTrue(mappedErr.isErr());
        assertEquals("error", mappedErr.unwrapErr());
    }

    @Test
    @DisplayName("mapErr should transform Err value and preserve Ok")
    void testMapErr() {
        var okResult = Result.<String, Integer>ok("success");
        var errResult = Result.<String, Integer>err(404);

        Function<Integer, String> mapper = i -> "error_" + i;

        var mappedOk = okResult.mapErr(mapper);
        var mappedErr = errResult.mapErr(mapper);

        assertTrue(mappedOk.isOk());
        assertEquals("success", mappedOk.unwrap());
        assertTrue(mappedErr.isErr());
        assertEquals("error_404", mappedErr.unwrapErr());
    }

    @Test
    @DisplayName("mapOr should return mapped value for Ok or default for Err")
    void testMapOr() {
        var okResult = Result.<Integer, String>ok(5);
        var errResult = Result.<Integer, String>err("error");
        Function<Integer, String> mapper = i -> "mapped_" + i;

        assertEquals("mapped_5", okResult.mapOr("default", mapper));
        assertEquals("default", errResult.mapOr("default", mapper));
    }

    @Test
    @DisplayName("mapOrElse should apply appropriate function based on result type")
    void testMapOrElse() {
        var okResult = Result.<Integer, String>ok(5);
        var errResult = Result.<Integer, String>err("error");

        Function<String, String> errMapper = err -> "err_" + err;
        Function<Integer, String> okMapper = i -> "ok_" + i;

        assertEquals("ok_5", okResult.mapOrElse(errMapper, okMapper));
        assertEquals("err_error", errResult.mapOrElse(errMapper, okMapper));
    }

    @Test
    @DisplayName("and should return second result if first is Ok, otherwise first Err")
    void testAnd() {
        var ok1 = Result.<String, String>ok("first");
        var ok2 = Result.<Integer, String>ok(42);
        var err1 = Result.<String, String>err("error1");
        var err2 = Result.<Integer, String>err("error2");

        assertTrue(ok1.and(ok2).isOk());
        assertEquals(42, ok1.and(ok2).unwrap());
        assertTrue(err1.and(ok2).isErr());
        assertEquals("error1", err1.and(ok2).unwrapErr());
        assertTrue(ok1.and(err2).isErr());
        assertEquals("error2", ok1.and(err2).unwrapErr());
    }

    @Test
    @DisplayName("andThen should chain operations for Ok and preserve Err")
    void testAndThen() {
        var okResult = Result.<Integer, String>ok(5);
        var errResult = Result.<Integer, String>err("error");

        Function<Integer, Result<String, String>> mapper = i -> Result.ok("result_" + i);

        var chainedOk = okResult.andThen(mapper);
        var chainedErr = errResult.andThen(mapper);

        assertTrue(chainedOk.isOk());
        assertEquals("result_5", chainedOk.unwrap());
        assertTrue(chainedErr.isErr());
        assertEquals("error", chainedErr.unwrapErr());
    }

    @Test
    @DisplayName("or should return first result if Ok, otherwise second result")
    void testOr() {
        var ok1 = Result.<String, String>ok("success");
        var ok2 = Result.<String, Integer>ok("backup");
        var err1 = Result.<String, String>err("error1");
        var err2 = Result.<String, Integer>err(404);

        assertTrue(ok1.or(ok2).isOk());
        assertEquals("success", ok1.or(ok2).unwrap());
        assertTrue(err1.or(ok2).isOk());
        assertEquals("backup", err1.or(ok2).unwrap());
        assertTrue(err1.or(err2).isErr());
        assertEquals(404, err1.or(err2).unwrapErr());
    }

    @Test
    @DisplayName("orElse should return first result if Ok, otherwise apply function to error")
    void testOrElse() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        Function<String, Result<String, Integer>> mapper = err -> Result.ok("recovered_" + err);

        var recoveredOk = okResult.orElse(mapper);
        var recoveredErr = errResult.orElse(mapper);

        assertTrue(recoveredOk.isOk());
        assertEquals("success", recoveredOk.unwrap());
        assertTrue(recoveredErr.isOk());
        assertEquals("recovered_error", recoveredErr.unwrap());
    }

    @Test
    @DisplayName("ifOk should execute function only for Ok results")
    void testIfOk() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");
        var executed = new boolean[1];

        Function<String, Void> sideEffect = value -> {
            executed[0] = true;
            return null;
        };

        executed[0] = false;
        okResult.ifOk(sideEffect);
        assertTrue(executed[0]);

        executed[0] = false;
        errResult.ifOk(sideEffect);
        assertFalse(executed[0]);
    }

    @Test
    @DisplayName("ifErr should execute function only for Err results")
    void testIfErr() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");
        var executed = new boolean[1];

        Function<String, Void> sideEffect = value -> {
            executed[0] = true;
            return null;
        };

        executed[0] = false;
        okResult.ifErr(sideEffect);
        assertFalse(executed[0]);

        executed[0] = false;
        errResult.ifErr(sideEffect);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("match should apply appropriate handler based on result type")
    void testMatch() {
        var okResult = Result.<Integer, String>ok(42);
        var errResult = Result.<Integer, String>err("error");

        Function<Integer, String> okHandler = i -> "ok_" + i;
        Function<String, String> errHandler = err -> "err_" + err;

        assertEquals("ok_42", okResult.match(okHandler, errHandler));
        assertEquals("err_error", errResult.match(okHandler, errHandler));
    }

    @Test
    @DisplayName("collect should combine all Ok values or return first Err")
    void testCollect() {
        var allOk = Result.collect(
                Result.ok("a"),
                Result.ok("b"),
                Result.ok("c")
        );
        assertTrue(allOk.isOk());
        assertEquals(List.of("a", "b", "c"), allOk.unwrap());

        var hasErr = Result.collect(
                Result.ok("a"),
                Result.err("error"),
                Result.ok("c")
        );
        assertTrue(hasErr.isErr());
        assertEquals("error", hasErr.unwrapErr());
    }

    @Test
    @DisplayName("unwrapOrThrow should return value for Ok and throw for Err")
    void testUnwrapOrThrow() throws Exception {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");

        assertEquals("success", okResult.unwrapOrThrow());

        assertThrows(Exception.class, errResult::unwrapOrThrow);
    }

    @Test
    @DisplayName("inspect should call function for Ok and return self")
    void testInspect() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");
        var inspected = new String[1];

        Function<String, Void> inspector = value -> {
            inspected[0] = value;
            return null;
        };

        var resultOk = okResult.inspect(inspector);
        assertSame(okResult, resultOk);
        assertEquals("success", inspected[0]);

        inspected[0] = null;
        var resultErr = errResult.inspect(inspector);
        assertSame(errResult, resultErr);
        assertNull(inspected[0]);
    }

    @Test
    @DisplayName("inspectErr should call function for Err and return self")
    void testInspectErr() {
        var okResult = Result.<String, String>ok("success");
        var errResult = Result.<String, String>err("error");
        var inspected = new String[1];

        Function<String, Void> inspector = value -> {
            inspected[0] = value;
            return null;
        };

        var resultOk = okResult.inspectErr(inspector);
        assertSame(okResult, resultOk);
        assertNull(inspected[0]);

        var resultErr = errResult.inspectErr(inspector);
        assertSame(errResult, resultErr);
        assertEquals("error", inspected[0]);
    }

    @ParameterizedTest
    @DisplayName("combine operations should handle various argument counts")
    @MethodSource("provideCombineTestCases")
    void testCombineOperations(CombineTestCase testCase) {
        var result = testCase.executeTest();
        
        if (testCase.shouldSucceed) {
            assertTrue(result.isOk(), "Expected Ok result for " + testCase.description);
            assertEquals(testCase.expectedValue, result.unwrap(), "Unexpected combined value for " + testCase.description);
        } else {
            assertTrue(result.isErr(), "Expected Err result for " + testCase.description);
            assertEquals(testCase.expectedError, result.unwrapErr(), "Unexpected error for " + testCase.description);
        }
    }

    static Stream<CombineTestCase> provideCombineTestCases() {

        var okA = Result.<String, String>ok("A");
        var okB = Result.<String, String>ok("B");
        var okC = Result.<String, String>ok("C");
        var okD = Result.<String, String>ok("D");
        var okE = Result.<String, String>ok("E");
        var okF = Result.<String, String>ok("F");
        var okG = Result.<String, String>ok("G");
        var okH = Result.<String, String>ok("H");
        var okI = Result.<String, String>ok("I");
        var okJ = Result.<String, String>ok("J");
        var okK = Result.<String, String>ok("K");
        var okL = Result.<String, String>ok("L");
        var okM = Result.<String, String>ok("M");
        var okN = Result.<String, String>ok("N");
        var okO = Result.<String, String>ok("O");
        
        var err = Result.<String, String>err("error");
        var err1 = Result.<String, String>err("error1");
        var err2 = Result.<String, String>err("error2");
        var firstErr = Result.<String, String>err("first_error");
        var secondErr = Result.<String, String>err("second_error");

        return Stream.of(
            // Combine1 tests
            new CombineTestCase("combine1 all ok", true, "A", null, () -> 
                Result.combine(okA).with(a -> a)),
            new CombineTestCase("combine1 with error", false, null, "error", () -> 
                Result.combine(err).with(a -> a)),
                
            // Combine2 tests
            new CombineTestCase("combine2 all ok", true, "AB", null, () -> 
                Result.combine(okA, okB).with((a, b) -> a + b)),
            new CombineTestCase("combine2 first error", false, null, "error1", () -> 
                Result.combine(err1, okB).with((a, b) -> a + b)),
            new CombineTestCase("combine2 second error", false, null, "error2", () -> 
                Result.combine(okA, err2).with((a, b) -> a + b)),
                
            // Combine3 tests
            new CombineTestCase("combine3 all ok", true, "ABC", null, () -> 
                Result.combine(okA, okB, okC).with((a, b, c) -> a + b + c)),
            new CombineTestCase("combine3 first error", false, null, "error", () -> 
                Result.combine(err, okB, okC).with((a, b, c) -> a + b + c)),
                
            // Combine4 tests
            new CombineTestCase("combine4 all ok", true, "ABCD", null, () -> 
                Result.combine(okA, okB, okC, okD).with((a, b, c, d) -> a + b + c + d)),
            new CombineTestCase("combine4 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD).with((a, b, c, d) -> a + b + c + d)),
                
            // Combine5 tests
            new CombineTestCase("combine5 all ok", true, "ABCDE", null, () -> 
                Result.combine(okA, okB, okC, okD, okE).with((a, b, c, d, e) -> a + b + c + d + e)),
            new CombineTestCase("combine5 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE).with((a, b, c, d, e) -> a + b + c + d + e)),
                
            // Combine6 tests
            new CombineTestCase("combine6 all ok", true, "ABCDEF", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF).with((a, b, c, d, e, f) -> a + b + c + d + e + f)),
            new CombineTestCase("combine6 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF).with((a, b, c, d, e, f) -> a + b + c + d + e + f)),
                
            // Combine7 tests
            new CombineTestCase("combine7 all ok", true, "ABCDEFG", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG).with((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g)),
            new CombineTestCase("combine7 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG).with((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g)),
                
            // Combine8 tests
            new CombineTestCase("combine8 all ok", true, "ABCDEFGH", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH).with((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h)),
            new CombineTestCase("combine8 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH).with((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h)),
                
            // Combine9 tests
            new CombineTestCase("combine9 all ok", true, "ABCDEFGHI", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI).with((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i)),
            new CombineTestCase("combine9 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI).with((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i)),
                
            // Combine10 tests
            new CombineTestCase("combine10 all ok", true, "ABCDEFGHIJ", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ).with((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j)),
            new CombineTestCase("combine10 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ).with((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j)),
                
            // Combine11 tests
            new CombineTestCase("combine11 all ok", true, "ABCDEFGHIJK", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK).with((a, b, c, d, e, f, g, h, i, j, k) -> a + b + c + d + e + f + g + h + i + j + k)),
            new CombineTestCase("combine11 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK).with((a, b, c, d, e, f, g, h, i, j, k) -> a + b + c + d + e + f + g + h + i + j + k)),
                
            // Combine12 tests
            new CombineTestCase("combine12 all ok", true, "ABCDEFGHIJKL", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL).with((a, b, c, d, e, f, g, h, i, j, k, l) -> a + b + c + d + e + f + g + h + i + j + k + l)),
            new CombineTestCase("combine12 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL).with((a, b, c, d, e, f, g, h, i, j, k, l) -> a + b + c + d + e + f + g + h + i + j + k + l)),
                
            // Combine13 tests
            new CombineTestCase("combine13 all ok", true, "ABCDEFGHIJKLM", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM).with((a, b, c, d, e, f, g, h, i, j, k, l, m) -> a + b + c + d + e + f + g + h + i + j + k + l + m)),
            new CombineTestCase("combine13 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM).with((a, b, c, d, e, f, g, h, i, j, k, l, m) -> a + b + c + d + e + f + g + h + i + j + k + l + m)),
                
            // Combine14 tests
            new CombineTestCase("combine14 all ok", true, "ABCDEFGHIJKLMN", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM, okN).with((a, b, c, d, e, f, g, h, i, j, k, l, m, n) -> a + b + c + d + e + f + g + h + i + j + k + l + m + n)),
            new CombineTestCase("combine14 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM, okN).with((a, b, c, d, e, f, g, h, i, j, k, l, m, n) -> a + b + c + d + e + f + g + h + i + j + k + l + m + n)),
                
            // Combine15 tests
            new CombineTestCase("combine15 all ok", true, "ABCDEFGHIJKLMNO", null, () -> 
                Result.combine(okA, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM, okN, okO).with((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) -> a + b + c + d + e + f + g + h + i + j + k + l + m + n + o)),
            new CombineTestCase("combine15 with error", false, null, "error", () -> 
                Result.combine(err, okB, okC, okD, okE, okF, okG, okH, okI, okJ, okK, okL, okM, okN, okO).with((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) -> a + b + c + d + e + f + g + h + i + j + k + l + m + n + o)),
                
            // Error precedence test
            new CombineTestCase("combine fail fast", false, null, "first_error", () -> 
                Result.combine(Result.<String, String>ok("A"), firstErr, secondErr).with((a, b, c) -> a + b + c))
        );
    }

    static class CombineTestCase {
        final String description;
        final boolean shouldSucceed;
        final String expectedValue;
        final String expectedError;
        final java.util.function.Supplier<Result<String, String>> testExecutor;

        CombineTestCase(String description, boolean shouldSucceed, String expectedValue, String expectedError, java.util.function.Supplier<Result<String, String>> testExecutor) {
            this.description = description;
            this.shouldSucceed = shouldSucceed;
            this.expectedValue = expectedValue;
            this.expectedError = expectedError;
            this.testExecutor = testExecutor;
        }

        Result<String, String> executeTest() {
            return testExecutor.get();
        }

        @Override
        public String toString() {
            return description;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "", "null"})
    @DisplayName("Result should work with different value types")
    void testDifferentValueTypes(String value) {
        var result = Result.<String, String>ok(value);
        assertTrue(result.isOk());
        assertEquals(value, result.unwrap());
    }

    @Test
    @DisplayName("Result should work with null values")
    void testNullValues() {
        var okWithNull = Result.<String, String>ok(null);
        var errWithNull = Result.<String, String>err(null);

        assertTrue(okWithNull.isOk());
        assertNull(okWithNull.unwrap());
        assertTrue(errWithNull.isErr());
        assertNull(errWithNull.unwrapErr());
    }

    @Test
    @DisplayName("Result should maintain type safety across transformations")
    void testTypeSafety() {
        Result<Integer, String> intResult = Result.ok(42);
        Result<String, String> stringResult = intResult.map(Object::toString);
        Result<String, Integer> mappedErrResult = stringResult.mapErr(String::length);

        assertTrue(stringResult.isOk());
        assertEquals("42", stringResult.unwrap());
    }
}