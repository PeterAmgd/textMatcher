package com.example.textMatcher.payloads.responses;


import com.example.textMatcher.model.MatchResult;

import java.util.List;

public record MatchingResponse(
        String referenceFile,
        MatchResult bestMatch,
        List<MatchResult> results
) {
}
