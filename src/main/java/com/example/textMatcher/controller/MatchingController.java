package com.example.textMatcher.controller;

import com.example.textMatcher.payloads.responses.MatchingResponse;
import com.example.textMatcher.service.FileMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final FileMatchingService fileMatchingService;

    @GetMapping
    public MatchingResponse matchFiles() {
        return fileMatchingService.findMatches();
    }
}
