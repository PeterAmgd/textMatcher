package com.example.textMatcher.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
@Component
public class SimilarityCalculator {

    public double calculate(
            Set<String> referenceWords,
            Set<String> candidateWords
    ) {

        if (referenceWords.isEmpty()
                && candidateWords.isEmpty()) {
            return 100.0;
        }

        if (referenceWords.isEmpty()
                || candidateWords.isEmpty()) {
            return 0.0;
        }

        Set<String> smallerSet;
        Set<String> largerSet;

        if (referenceWords.size() <= candidateWords.size()) {
            smallerSet = referenceWords;
            largerSet = candidateWords;
        } else {
            smallerSet = candidateWords;
            largerSet = referenceWords;
        }

        int intersectionSize = 0;

        for (String word : smallerSet) {

            if (largerSet.contains(word)) {
                intersectionSize++;
            }
        }

        int unionSize =
                referenceWords.size()
                        + candidateWords.size()
                        - intersectionSize;

        return (double) intersectionSize
                / unionSize
                * 100.0;
    }
}