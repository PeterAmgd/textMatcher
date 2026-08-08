# textMatcher# Text Matcher

A performance-oriented Spring Boot application that compares a reference text file against a pool of text files and calculates a similarity score for each candidate file.

The application is designed to efficiently process large text files while supporting concurrent comparison of multiple files.

---

## 1. Features

* Read the reference file path from configuration.
* Read the candidate files directory from configuration.
* Compare the reference file against all files in the pool.
* Ignore word ordering.
* Ignore duplicate words.
* Normalize words to lowercase.
* Accept only tokens containing alphabetic characters.
* Calculate a similarity score from `0%` to `100%`.
* Return results sorted by similarity score descending.
* Identify the best matching file.
* Process candidate files concurrently using a bounded `ExecutorService`.
* Read files incrementally using `BufferedReader` to reduce memory consumption.
* Configurable maximum parallelism.
* REST API.
* Unit and integration tests.
* Docker support.

---

## 2. Technology Stack

* Java 17
* Spring Boot
* Maven
* Spring Boot Test
* Docker
* Docker Compose

---

## 3. Project Structure

```text
text-matcher/
│
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
│
├── testingFiles/
│   ├── reference.txt
│   └── pool/
│       ├── file1.txt
│       ├── file2.txt
│       └── ...
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/textMatcher/
    │   │       ├── config/
    │   │       ├── controller/
    │   │       ├── model/
    │   │       ├── reader/
    │   │       └── service/
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/
        ├── java/
        └── resources/
```

---

## 4. Requirements

### Required Software

* JDK 17+
* Maven 3.8+
* Docker (optional)

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

# 5. Configuration

The application receives the reference file and pool directory through configuration.

Example `application.yml`:

```yaml
spring:
  application:
    name: text-matcher

server:
  port: ${SERVER_PORT:8081}

file-matcher:
  reference-file: ${FILE_MATCHER_REFERENCE_FILE:./testingFiles/reference.txt}
  pool-directory: ${FILE_MATCHER_POOL_DIRECTORY:./testingFiles/pool}
  max-parallel-files: ${FILE_MATCHER_MAX_PARALLEL_FILES:4}
```

### Configuration Properties

| Property                          | Description                                    | Default                        |
| --------------------------------- | ---------------------------------------------- | ------------------------------ |
| `file-matcher.reference-file`     | Path to the reference file                     | `./testingFiles/reference.txt` |
| `file-matcher.pool-directory`     | Directory containing candidate files           | `./testingFiles/pool`          |
| `file-matcher.max-parallel-files` | Maximum number of files processed concurrently | `4`                            |
| `server.port`                     | HTTP server port                               | `8081`                         |

The file paths are intentionally configurable because the task requires the application to read them from its properties/configuration.

---

# 6. Input File Structure

The recommended local structure is:

```text
text-matcher/
│
├── testingFiles/
│   ├── reference.txt
│   └── pool/
│       ├── perfect.txt
│       ├── partial.txt
│       ├── half.txt
│       ├── no-match.txt
│       ├── extra-words.txt
│       ├── duplicates.txt
│       └── invalid-tokens.txt
```

The application treats every regular file inside the configured pool directory as a candidate file.

---

# 7. Word Definition

A chunk of text is considered a word only when the complete token consists of alphabetic characters.

Examples:

```text
Hello        -> valid
Java         -> valid
مصر          -> valid
こんにちは      -> valid

hello123     -> invalid
spring-boot  -> invalid
java_17      -> invalid
hello!       -> invalid
```

The implementation uses Unicode-aware alphabetic character detection.

Words are normalized to lowercase before comparison.

For example:

```text
Java
JAVA
java
```

are treated as the same word.

---

# 8. Duplicate Words

Duplicate words are counted only once.

For example:

```text
Java Java Java Spring Spring Boot
```

becomes:

```text
java
spring
boot
```

This is intentional because the requirement compares the words contained in the files and explicitly disregards ordering.

---

# 9. Similarity Algorithm

The application uses **Jaccard similarity** between the sets of unique words.

Let:

```text
A = unique words in the reference file
B = unique words in the candidate file
```

The similarity is:

```text
|A ∩ B|
──────────── × 100
|A ∪ B|
```

Where:

* `A ∩ B` = words shared by both files.
* `A ∪ B` = all unique words appearing in either file.

### Examples

#### Exact Match

```text
Reference:
The quick brown fox

Candidate:
fox brown quick The
```

The two sets are identical.

```text
Score = 100%
```

#### No Match

```text
Reference:
Java Spring Boot

Candidate:
Python Django Flask
```

There are no common words.

```text
Score = 0%
```

#### Partial Match

```text
Reference:
java spring boot docker

Candidate:
java spring python
```

Common words:

```text
java
spring
```

Union:

```text
java
spring
boot
docker
python
```

Therefore:

```text
Score = 2 / 5 × 100 = 40%
```

---


# 10. Performance Considerations

The task allows files containing up to approximately 10 million words.

To handle large files efficiently, the application does not load the entire file contents into a single `String`.

Instead, files are read incrementally using:

```java
BufferedReader
```

This significantly reduces unnecessary memory allocation.

### Reference File

The reference file is read only once.

Its resulting set of unique words is then reused for every candidate comparison.

This avoids repeatedly reading and parsing the same file.


# 11. Complexity

For a reference set `A` and candidate set `B`:

### Reading

The file contents must be read at least once:

```text
O(file size)
```

### Similarity

The implementation iterates over the smaller set and performs hash-based lookups:

```text
O(min(|A|, |B|))
```

approximately, assuming average `O(1)` HashSet lookup.

### Memory

The application stores the unique words rather than the complete file contents:

```text
O(|A| + |B|)
```

For concurrent processing, the maximum number of active candidate sets is bounded by the configured thread-pool size.

---

# 12. REST API

## Get Matching Results

### Request

```http
GET /api/matching
```

### Example

```bash
curl http://localhost:8081/api/matching
```

### Example Response

```json
{
  "referenceFile": "reference.txt",
  "bestMatch": {
    "fileName": "perfect.txt",
    "score": 100.0
  },
  "results": [
    {
      "fileName": "duplicates.txt",
      "score": 100.0
    },
    {
      "fileName": "perfect.txt",
      "score": 100.0
    },
    {
      "fileName": "extra-words.txt",
      "score": 72.73
    },
    {
      "fileName": "partial.txt",
      "score": 66.67
    },
    {
      "fileName": "invalid-tokens.txt",
      "score": 50.0
    },
    {
      "fileName": "half.txt",
      "score": 25.0
    },
    {
      "fileName": "no-match.txt",
      "score": 0.0
    }
  ]
}
```

Results are sorted by:

1. Score descending.
2. File name ascending when scores are equal.

This makes the response deterministic.

---

# 13. Best Match

The `bestMatch` field contains the first result after sorting.

If multiple files have the same highest score, the file name is used as a deterministic tie-breaker.

For example:

```text
duplicates.txt -> 100%
perfect.txt    -> 100%
```

The result is:

```text
duplicates.txt
```

because it comes first alphabetically.

The specification does not define a special tie-breaking rule, so deterministic filename ordering is used.

---

# 14. Error Handling

The application validates the configured paths before processing.

Examples of invalid configuration:

```text
Reference file does not exist
Pool directory does not exist
```

The API returns an error response describing the problem.

Example:

```json
{
  "error": "Reference file does not exist: ./testingFiles/reference.txt"
}
```

---

# 15. Running Locally

From the project root:

```bash
mvnw.cmd clean test
```

Then:

```bash
mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

Test the API:

```bash
curl http://localhost:8081/api/matching
```

---

# 17. Building the Application

```bash
mvnw.cmd clean package
```

The generated JAR will be available under:

```text
target/
```

---

# 16. Docker

Docker is optional but supported.

Build and start the application:

```bash
docker compose up --build
```

The application will be available at:

```text
http://localhost:8081
```

The Docker configuration mounts the local testing files into the container so that the configured paths remain external to the application image.

---



# 17. Assumptions

The following assumptions were made where the specification does not explicitly define behavior:

1. Word comparison is case-insensitive.
2. Duplicate words are counted once.
3. A word must consist entirely of alphabetic Unicode characters.
4. Files are interpreted as UTF-8.
5. Only regular files directly inside the configured pool directory are processed.
6. Results are sorted by score descending and then file name ascending.
7. If both reference and candidate sets are empty, they are considered identical and receive `100%`.
8. When multiple files have the highest score, deterministic filename ordering is used to select the best match.

---


# 18. Summary

This application provides a configurable and efficient way to compare a reference text file against a pool of candidate files.

The implementation focuses on:

* Correctness.
* Memory-conscious file processing.
* Concurrent execution.
* Deterministic results.
* Clean separation of responsibilities.
* Testability.
* Containerization.
* API documentation.

The solution satisfies the core requirements while keeping the implementation simple and extensible.
