package com.example.textMatcher.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "file-matcher")
public record FileMatcherProperties(

        @NotBlank
        String referenceFile,

        @NotBlank
        String poolDirectory,

        @Min(1)
        int maxParallelFiles
) {
}
