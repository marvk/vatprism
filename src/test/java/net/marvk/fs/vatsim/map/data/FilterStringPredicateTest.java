package net.marvk.fs.vatsim.map.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class FilterStringPredicateTest {
    @Test
    void testValidRegex() {
        final Optional<Filter.StringPredicate> test = Filter.StringPredicate.tryCreate("AAL.*", true);
        final Filter.StringPredicate predicate = Assertions.assertDoesNotThrow(test::get);

        runAalTest(predicate);
    }

    @Test
    void testValidStartOfLine() {
        final Optional<Filter.StringPredicate> test = Filter.StringPredicate.tryCreate("^AAL.*", true);
        final Filter.StringPredicate predicate = Assertions.assertDoesNotThrow(test::get);

        runAalTest(predicate);
    }

    @Test
    void testValidEndOfLine() {
        final Optional<Filter.StringPredicate> test = Filter.StringPredicate.tryCreate("AAL.*$", true);
        final Filter.StringPredicate predicate = Assertions.assertDoesNotThrow(test::get);

        runAalTest(predicate);
    }

    @Test
    void testInvalidRegex() {
        final Optional<Filter.StringPredicate> test = Filter.StringPredicate.tryCreate("AAL[", false);
        Assertions.assertFalse(test::isPresent);
    }

    @Test
    void testValidSimple() {
        final Optional<Filter.StringPredicate> test = Filter.StringPredicate.tryCreate("AAL*", false);
        final Filter.StringPredicate predicate = Assertions.assertDoesNotThrow(test::get);

        runAalTest(predicate);
    }

    private void runAalTest(final Filter.StringPredicate predicate) {
        Assertions.assertTrue(predicate.test("AAL"));
        Assertions.assertTrue(predicate.test("AAL123"));
        Assertions.assertTrue(predicate.test("AALFOO"));
        Assertions.assertFalse(predicate.test("\nAAL"));
        Assertions.assertFalse(predicate.test("FOO"));
        Assertions.assertFalse(predicate.test(""));
    }
}