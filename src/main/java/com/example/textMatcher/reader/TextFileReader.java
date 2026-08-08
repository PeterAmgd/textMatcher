package com.example.textMatcher.reader;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class TextFileReader {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("\\S+");

    public Set<String> readUniqueWords(Path file) throws IOException {

        Set<String> words = new HashSet<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             file,
                             StandardCharsets.UTF_8
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                Matcher matcher =
                        TOKEN_PATTERN.matcher(line);

                while (matcher.find()) {

                    String token = matcher.group();

                    if (isAlphabetic(token)) {
                        words.add(token.toLowerCase());
                    }
                }
            }
        }

        return words;
    }

    private boolean isAlphabetic(String token) {

        if (token.isEmpty()) {
            return false;
        }

        return token.codePoints()
                .allMatch(Character::isAlphabetic);
    }
}