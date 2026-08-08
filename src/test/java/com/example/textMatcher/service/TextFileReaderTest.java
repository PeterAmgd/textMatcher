package com.example.textMatcher.service;

import com.example.textMatcher.reader.TextFileReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFileReaderTest {

    private final TextFileReader reader =
            new TextFileReader();

    @Test
    void shouldExtractAlphabeticWordsOnly(
            @TempDir Path tempDir
    ) throws Exception {

        Path file = tempDir.resolve("test.txt");

        Files.writeString(
                file,
                "Hello hello Java123 spring-world Docker!"
        );

        Set<String> words =
                reader.readUniqueWords(file);

        assertEquals(
                Set.of("hello", "docker"),
                words
        );
    }
}
