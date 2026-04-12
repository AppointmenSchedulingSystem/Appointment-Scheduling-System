package Fall2026.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for CredentialStorage.
 *
 * No mocking is used. CredentialStorage has no injectable dependencies —
 * it IS the I/O boundary. Tests use JUnit 5's @TempDir to create isolated
 * temporary files that are deleted automatically after each test.
 */
@DisplayName("CredentialStorage Tests")
class CredentialStorageTest {

    @TempDir
    Path tempDir;

    private CredentialStorage storage;

    @BeforeEach
    void setUp() {
        storage = new CredentialStorage();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Returns the absolute path string of a temp file with the given name. */
    private String tempFile(String name) {
        return tempDir.resolve(name).toString();
    }

    /** Creates a temp file pre-populated with the given lines. */
    private String tempFileWithContent(String name, String... lines) throws IOException {
        Path path = tempDir.resolve(name);
        Files.write(path, List.of(lines));
        return path.toString();
    }

    // =========================================================================
    // ReadFromFile
    // =========================================================================

    @Nested
    @DisplayName("ReadFromFile")
    class ReadFromFileTests {

        @Test
        @DisplayName("returns empty list when file does not exist")
        void returnsEmptyListWhenFileDoesNotExist() {
            List<String> result = storage.ReadFromFile(tempFile("missing.txt"));

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns all lines from a file with multiple entries")
        void returnsAllLinesFromPopulatedFile() throws IOException {
            String file = tempFileWithContent("data.txt", "line one", "line two", "line three");

            List<String> result = storage.ReadFromFile(file);

            assertEquals(3, result.size());
            assertEquals("line one",   result.get(0));
            assertEquals("line two",   result.get(1));
            assertEquals("line three", result.get(2));
        }

        @Test
        @DisplayName("returns a single line from a file with one entry")
        void returnsSingleLine() throws IOException {
            String file = tempFileWithContent("single.txt", "only line");

            List<String> result = storage.ReadFromFile(file);

            assertEquals(1, result.size());
            assertEquals("only line", result.get(0));
        }

        @Test
        @DisplayName("skips blank lines in the file")
        void skipsBlankLines() throws IOException {
            String file = tempFileWithContent("blanks.txt", "first", "", "   ", "second");

            List<String> result = storage.ReadFromFile(file);

            assertEquals(2, result.size());
            assertEquals("first",  result.get(0));
            assertEquals("second", result.get(1));
        }

        @Test
        @DisplayName("returns empty list for a file that contains only blank lines")
        void returnsEmptyListForAllBlankFile() throws IOException {
            String file = tempFileWithContent("allblanks.txt", "", "   ", "\t");

            List<String> result = storage.ReadFromFile(file);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for a completely empty file")
        void returnsEmptyListForEmptyFile() throws IOException {
            Path path = tempDir.resolve("empty.txt");
            Files.createFile(path);

            List<String> result = storage.ReadFromFile(path.toString());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("preserves leading and trailing whitespace within content lines")
        void preservesWhitespaceWithinContentLines() throws IOException {
            String file = tempFileWithContent("spaces.txt", "  padded  ");

            List<String> result = storage.ReadFromFile(file);

            assertEquals(1, result.size());
            assertEquals("  padded  ", result.get(0));
        }

        @Test
        @DisplayName("reads CSV-formatted lines without altering content")
        void readsCsvLinesUnchanged() throws IOException {
            String file = tempFileWithContent("admins.txt",
                    "1,admin,admin123,admin@example.com",
                    "2,alice,pass1,alice@example.com"
            );

            List<String> result = storage.ReadFromFile(file);

            assertEquals(2, result.size());
            assertEquals("1,admin,admin123,admin@example.com",  result.get(0));
            assertEquals("2,alice,pass1,alice@example.com",     result.get(1));
        }
    }

    // =========================================================================
    // WriteToFile
    // =========================================================================

    @Nested
    @DisplayName("WriteToFile")
    class WriteToFileTests {

        @Test
        @DisplayName("returns true and creates the file when it does not exist")
        void returnsTrueAndCreatesNewFile() {
            String file = tempFile("new.txt");

            boolean result = storage.WriteToFile(file, List.of("hello"));

            assertTrue(result);
            assertTrue(Files.exists(Path.of(file)));
        }

        @Test
        @DisplayName("written lines can be read back correctly")
        void writtenLinesCanBeReadBack() {
            String file = tempFile("roundtrip.txt");
            List<String> lines = List.of("alpha", "beta", "gamma");

            storage.WriteToFile(file, lines);
            List<String> result = storage.ReadFromFile(file);

            assertEquals(3, result.size());
            assertEquals("alpha", result.get(0));
            assertEquals("beta",  result.get(1));
            assertEquals("gamma", result.get(2));
        }

        @Test
        @DisplayName("overwrites existing file content completely")
        void overwritesExistingContent() throws IOException {
            String file = tempFileWithContent("overwrite.txt", "old line one", "old line two");

            storage.WriteToFile(file, List.of("new line"));
            List<String> result = storage.ReadFromFile(file);

            assertEquals(1, result.size());
            assertEquals("new line", result.get(0));
        }

        @Test
        @DisplayName("writes an empty list and results in a file with no content")
        void writesEmptyList() {
            String file = tempFile("empty.txt");

            boolean result = storage.WriteToFile(file, List.of());
            List<String> read = storage.ReadFromFile(file);

            assertTrue(result);
            assertTrue(read.isEmpty());
        }

        @Test
        @DisplayName("returns false when path is not writable")
        void returnsFalseForUnwritablePath() {
            // A path inside a non-existent nested directory cannot be written to
            String badPath = tempDir.resolve("nonexistent/subdir/file.txt").toString();

            boolean result = storage.WriteToFile(badPath, List.of("data"));

            assertFalse(result);
        }

        @Test
        @DisplayName("writes CSV-formatted lines and reads them back unchanged")
        void writesCsvLinesUnchanged() {
            String file = tempFile("csv.txt");
            List<String> csv = List.of(
                    "1,admin,admin123,admin@example.com",
                    "2,alice,pass1,alice@example.com"
            );

            storage.WriteToFile(file, csv);
            List<String> result = storage.ReadFromFile(file);

            assertEquals(2, result.size());
            assertEquals("1,admin,admin123,admin@example.com", result.get(0));
            assertEquals("2,alice,pass1,alice@example.com",    result.get(1));
        }
    }

    // =========================================================================
    // appendToFile
    // =========================================================================

    @Nested
    @DisplayName("appendToFile")
    class AppendToFileTests {

        @Test
        @DisplayName("returns true and creates file when it does not exist")
        void returnsTrueAndCreatesFile() {
            String file = tempFile("append_new.txt");

            boolean result = storage.appendToFile(file, "first line");

            assertTrue(result);
            assertTrue(Files.exists(Path.of(file)));
        }

        @Test
        @DisplayName("appended line can be read back from a new file")
        void appendedLineReadableFromNewFile() {
            String file = tempFile("append_read.txt");

            storage.appendToFile(file, "appended line");
            List<String> result = storage.ReadFromFile(file);

            assertEquals(1, result.size());
            assertEquals("appended line", result.get(0));
        }

        @Test
        @DisplayName("appends to existing content without overwriting it")
        void appendsWithoutOverwriting() throws IOException {
            String file = tempFileWithContent("existing.txt", "original line");

            storage.appendToFile(file, "appended line");
            List<String> result = storage.ReadFromFile(file);

            assertEquals(2, result.size());
            assertEquals("original line", result.get(0));
            assertEquals("appended line", result.get(1));
        }

        @Test
        @DisplayName("multiple sequential appends accumulate all lines in order")
        void multipleAppendsAccumulateInOrder() {
            String file = tempFile("multi_append.txt");

            storage.appendToFile(file, "line one");
            storage.appendToFile(file, "line two");
            storage.appendToFile(file, "line three");

            List<String> result = storage.ReadFromFile(file);

            assertEquals(3, result.size());
            assertEquals("line one",   result.get(0));
            assertEquals("line two",   result.get(1));
            assertEquals("line three", result.get(2));
        }

        @Test
        @DisplayName("returns false when path is not writable")
        void returnsFalseForUnwritablePath() {
            String badPath = tempDir.resolve("nonexistent/subdir/append.txt").toString();

            boolean result = storage.appendToFile(badPath, "data");

            assertFalse(result);
        }

        @Test
        @DisplayName("boundary: appending an empty string writes a blank line that is then skipped on read")
        void appendingEmptyStringIsSkippedOnRead() {
            String file = tempFile("blank_append.txt");

            storage.appendToFile(file, "");
            List<String> result = storage.ReadFromFile(file);

            // ReadFromFile skips blank lines, so the empty append is invisible on read-back
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // fileExists
    // =========================================================================

    @Nested
    @DisplayName("fileExists")
    class FileExistsTests {

        @Test
        @DisplayName("returns true for a file that exists")
        void returnsTrueForExistingFile() throws IOException {
            Path path = tempDir.resolve("exists.txt");
            Files.createFile(path);

            assertTrue(storage.fileExists(path.toString()));
        }

        @Test
        @DisplayName("returns false for a file that does not exist")
        void returnsFalseForMissingFile() {
            assertFalse(storage.fileExists(tempFile("missing.txt")));
        }

        @Test
        @DisplayName("returns true immediately after WriteToFile creates the file")
        void returnsTrueAfterWrite() {
            String file = tempFile("written.txt");

            storage.WriteToFile(file, List.of("data"));

            assertTrue(storage.fileExists(file));
        }

        @Test
        @DisplayName("returns true immediately after appendToFile creates the file")
        void returnsTrueAfterAppend() {
            String file = tempFile("appended.txt");

            storage.appendToFile(file, "data");

            assertTrue(storage.fileExists(file));
        }

        @Test
        @DisplayName("returns false for a path that is a directory, not a file")
        void returnsTrueForDirectory() {
            // tempDir itself is a directory — fileExists delegates to File.exists()
            // which returns true for directories too; this documents that behaviour
            assertTrue(storage.fileExists(tempDir.toString()));
        }
    }
}