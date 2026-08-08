package com.example.textMatcher.service;
import com.example.textMatcher.config.FileMatcherProperties;
import com.example.textMatcher.model.MatchResult;
import com.example.textMatcher.payloads.responses.MatchingResponse;
import com.example.textMatcher.reader.TextFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMatchingService {

    private final FileMatcherProperties properties;
    private final TextFileReader textFileReader;
    private final SimilarityCalculator similarityCalculator;
    private final ExecutorService fileMatcherExecutor;

    public MatchingResponse findMatches() {

        Path referencePath =
                Path.of(properties.referenceFile());

        Path poolPath =
                Path.of(properties.poolDirectory());

        validatePaths(referencePath, poolPath);

        try {

            log.info(
                    "Reading reference file: {}",
                    referencePath
            );

            Set<String> referenceWords =
                    textFileReader.readUniqueWords(referencePath);

            List<Path> poolFiles;

            try (var files = Files.list(poolPath)) {

                poolFiles = files
                        .filter(Files::isRegularFile)
                        .sorted()
                        .toList();
            }

            if (poolFiles.isEmpty()) {

                return new MatchingResponse(
                        referencePath
                                .getFileName()
                                .toString(),
                        null,
                        List.of()
                );
            }

            List<MatchResult> results =
                    processPoolFiles(
                            poolFiles,
                            referenceWords
                    );

            results.sort(
                    Comparator
                            .comparingDouble(
                                    MatchResult::score
                            )
                            .reversed()
                            .thenComparing(
                                    MatchResult::fileName
                            )
            );

            return new MatchingResponse(
                    referencePath
                            .getFileName()
                            .toString(),
                    results.get(0),
                    results
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to read matching files",
                    e
            );
        }
    }

    private List<MatchResult> processPoolFiles(
            List<Path> poolFiles,
            Set<String> referenceWords
    ) {

        List<Future<MatchResult>> futures =
                new ArrayList<>();

        for (Path file : poolFiles) {

            futures.add(
                    fileMatcherExecutor.submit(
                            () -> processFile(
                                    file,
                                    referenceWords
                            )
                    )
            );
        }

        List<MatchResult> results =
                new ArrayList<>();

        for (Future<MatchResult> future : futures) {

            try {
                results.add(future.get());

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "File processing was interrupted",
                        e
                );

            } catch (ExecutionException e) {

                throw new IllegalStateException(
                        "Failed to process candidate file",
                        e.getCause()
                );
            }
        }

        return results;
    }

    private MatchResult processFile(
            Path file,
            Set<String> referenceWords
    ) throws IOException {

        log.debug(
                "Processing candidate file: {}",
                file
        );

        Set<String> candidateWords =
                textFileReader.readUniqueWords(file);

        double score =
                similarityCalculator.calculate(
                        referenceWords,
                        candidateWords
                );

        double roundedScore =
                Math.round(score * 100.0) / 100.0;

        return new MatchResult(
                file.getFileName().toString(),
                roundedScore
        );
    }

    private void validatePaths(
            Path referencePath,
            Path poolPath
    ) {

        if (!Files.isRegularFile(referencePath)) {

            throw new IllegalArgumentException(
                    "Reference file does not exist: "
                            + referencePath
            );
        }

        if (!Files.isDirectory(poolPath)) {

            throw new IllegalArgumentException(
                    "Pool directory does not exist: "
                            + poolPath
            );
        }
    }
}