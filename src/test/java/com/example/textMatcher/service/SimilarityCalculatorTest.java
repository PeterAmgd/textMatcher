package com.example.textMatcher.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimilarityCalculatorTest {

    private final SimilarityCalculator calculator =
            new SimilarityCalculator();

    @Test
    void shouldReturn100ForExactMatch() {

        Set<String> a =
                Set.of("the", "quick", "brown", "fox");

        Set<String> b =
                Set.of("the", "quick", "brown", "fox");

        assertEquals(
                100.0,
                calculator.calculate(a, b)
        );
    }

    @Test
    void shouldIgnoreWordOrdering() {

        Set<String> a =
                Set.of("the", "quick", "brown", "fox");

        Set<String> b =
                Set.of("fox", "brown", "quick", "the");

        assertEquals(
                100.0,
                calculator.calculate(a, b)
        );
    }

    @Test
    void shouldReturnZeroWhenThereIsNoMatch() {

        Set<String> a =
                Set.of("java", "spring");

        Set<String> b =
                Set.of("python", "docker");

        assertEquals(
                0.0,
                calculator.calculate(a, b)
        );
    }

    @Test
    void shouldCalculatePartialMatch() {

        Set<String> a =
                Set.of("java", "spring", "boot");

        Set<String> b =
                Set.of("java", "spring", "docker");

        assertEquals(
                50.0,
                calculator.calculate(a, b)
        );
    }

    @Test
    void shouldReturn100ForTwoEmptySets() {

        assertEquals(
                100.0,
                calculator.calculate(
                        Set.of(),
                        Set.of()
                )
        );
    }
}
