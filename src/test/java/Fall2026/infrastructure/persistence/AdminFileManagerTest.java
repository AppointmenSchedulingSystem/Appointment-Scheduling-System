package Fall2026.infrastructure.persistence;

import Fall2026.domain.account.Admin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminFileManager.
 *
 * Mock strategy:
 * - CredentialStorage is always mocked — it performs real file I/O.
 * - Admin is a pure domain class and is always instantiated directly.
 *
 * ⚠ Requires one source change to be testable:
 *   Add a package-private constructor to AdminFileManager that accepts
 *   a CredentialStorage parameter so the mock can be injected:
 *
 *     AdminFileManager(CredentialStorage storage) {
 *         this.storage = storage;
 *         loadAdminsFromFile();
 *         if (admins.isEmpty()) {
 *             createDefaultAdmin();
 *         }
 *     }
 *
 *   This is the standard dependency-injection seam for unit testing.
 *   The public no-arg constructor remains unchanged for production use.
 */
@DisplayName("AdminFileManager Tests")
class AdminFileManagerTest {

    private static final String ADMIN_FILE = "Admins.txt";

    @Mock
    private CredentialStorage storage;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        reset(storage);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Builds a valid CSV line as AdminFileManager writes it. */
    private String adminLine(int id, String username, String password, String email) {
        return id + "," + username + "," + password + "," + email;
    }

    /** Creates an AdminFileManager with the mock storage injected. */
    private AdminFileManager buildManager() {
        return new AdminFileManager(storage);
    }

    // =========================================================================
    // Constructor — file has data
    // =========================================================================

    @Nested
    @DisplayName("Constructor — file has data")
    class ConstructorWithDataTests {

        @Test
        @DisplayName("loads a single valid admin line from file")
        void loadsOneAdmin() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));

            AdminFileManager manager = buildManager();

            Admin found = manager.findAdmin("alice");
            assertNotNull(found);
            assertEquals(1,                   found.getID());
            assertEquals("alice",             found.getUsername());
            assertEquals("pass1",             found.getPassword());
            assertEquals("alice@example.com", found.getEmail());
        }

        @Test
        @DisplayName("loads multiple valid admin lines from file")
        void loadsMultipleAdmins() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(List.of(
                    adminLine(1, "alice", "pass1", "alice@example.com"),
                    adminLine(2, "bob",   "pass2", "bob@example.com")
            ));

            AdminFileManager manager = buildManager();

            assertNotNull(manager.findAdmin("alice"));
            assertNotNull(manager.findAdmin("bob"));
        }

        @Test
        @DisplayName("does not create default admin when file already has admins")
        void doesNotWriteDefaultWhenFileHasData() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));

            buildManager();

            // WriteToFile must never be called — no default admin was needed
            verify(storage, times(0)).WriteToFile(eq(ADMIN_FILE), argThat(lines -> !lines.isEmpty()));
        }
    }

    // =========================================================================
    // Constructor — file is empty (default admin path)
    // =========================================================================

    @Nested
    @DisplayName("Constructor — file is empty")
    class ConstructorEmptyFileTests {

        @Test
        @DisplayName("creates default admin with id=1 when file is empty")
        void createsDefaultAdminWhenFileEmpty() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);

            AdminFileManager manager = buildManager();

            Admin defaultAdmin = manager.findAdmin("admin");
            assertNotNull(defaultAdmin);
            assertEquals(1,                   defaultAdmin.getID());
            assertEquals("admin",             defaultAdmin.getUsername());
            assertEquals("admin123",          defaultAdmin.getPassword());
            assertEquals("admin@example.com", defaultAdmin.getEmail());
        }

        @Test
        @DisplayName("saves default admin to file when file is empty")
        void savesDefaultAdminToFileWhenEmpty() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);

            buildManager();

            verify(storage).WriteToFile(
                    eq(ADMIN_FILE),
                    argThat(lines ->
                            lines.size() == 1 &&
                                    lines.get(0).equals(adminLine(1, "admin", "admin123", "admin@example.com"))
                    )
            );
        }
    }

    // =========================================================================
    // Constructor — malformed lines
    // =========================================================================

    @Nested
    @DisplayName("Constructor — malformed file lines")
    class ConstructorMalformedLineTests {

        @Test
        @DisplayName("skips line that has fewer than 4 comma-separated parts")
        void skipsLineTooFewParts() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of("alice,pass1,alice@example.com")); // only 3 parts

            AdminFileManager manager = buildManager();

            // Malformed line was skipped → admins list is empty → default admin created
            assertNotNull(manager.findAdmin("admin"), "Default admin should exist after skipping bad line");
            assertNull(manager.findAdmin("alice"),    "Malformed admin should not have been loaded");
        }

        @Test
        @DisplayName("skips line that has more than 4 comma-separated parts")
        void skipsLineTooManyParts() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of("1,alice,pass1,alice@example.com,extraField"));

            AdminFileManager manager = buildManager();

            assertNull(manager.findAdmin("alice"), "Line with 5 parts should be skipped");
        }

        @Test
        @DisplayName("skips empty line gracefully")
        void skipsEmptyLine() {
            // CredentialStorage already strips empty lines, but double-guard:
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);

            AdminFileManager manager = buildManager();

            assertNotNull(manager.findAdmin("admin"), "Default admin should exist");
        }

        @Test
        @DisplayName("loads valid lines and skips invalid ones in the same file")
        void loadsValidAndSkipsInvalid() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(List.of(
                    adminLine(1, "alice", "pass1", "alice@example.com"), // valid
                    "baddata",                                            // invalid — 1 part
                    adminLine(2, "bob",   "pass2", "bob@example.com")    // valid
            ));

            AdminFileManager manager = buildManager();

            assertNotNull(manager.findAdmin("alice"));
            assertNotNull(manager.findAdmin("bob"));
            assertNull(manager.findAdmin("baddata"));
        }
    }

    // =========================================================================
    // findAdmin
    // =========================================================================

    @Nested
    @DisplayName("findAdmin")
    class FindAdminTests {

        @Test
        @DisplayName("returns the correct admin for an existing username")
        void returnsCorrectAdminForExistingUsername() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(List.of(
                    adminLine(1, "alice", "pass1", "alice@example.com"),
                    adminLine(2, "bob",   "pass2", "bob@example.com")
            ));
            AdminFileManager manager = buildManager();

            Admin result = manager.findAdmin("bob");

            assertNotNull(result);
            assertEquals(2,                 result.getID());
            assertEquals("bob",             result.getUsername());
            assertEquals("bob@example.com", result.getEmail());
        }

        @Test
        @DisplayName("returns null when username does not exist")
        void returnsNullForNonExistentUsername() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));
            AdminFileManager manager = buildManager();

            Admin result = manager.findAdmin("charlie");

            assertNull(result);
        }

        @Test
        @DisplayName("returns null when admin list is empty")
        void returnsNullWhenListIsEmpty() {
            // Provide a single seeded admin so we bypass default-admin creation,
            // then search for someone who was never added.
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));
            AdminFileManager manager = buildManager();

            assertNull(manager.findAdmin("nobody"));
        }

        @Test
        @DisplayName("username match is case-sensitive")
        void usernameMatchIsCaseSensitive() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "Alice", "pass1", "alice@example.com")));
            AdminFileManager manager = buildManager();

            assertNull(manager.findAdmin("alice"),  "Lowercase 'alice' should not match stored 'Alice'");
            assertNotNull(manager.findAdmin("Alice"), "Exact-case 'Alice' should match");
        }
    }

    // =========================================================================
    // addNewAdmin
    // =========================================================================

    @Nested
    @DisplayName("addNewAdmin")
    class AddNewAdminTests {

        @Test
        @DisplayName("adds new admin and persists to file")
        void addsNewAdminAndSavesToFile() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            AdminFileManager manager = buildManager();

            manager.addNewAdmin("bob", "pass2", "bob@example.com");

            Admin result = manager.findAdmin("bob");
            assertNotNull(result);
            assertEquals("bob",             result.getUsername());
            assertEquals("pass2",           result.getPassword());
            assertEquals("bob@example.com", result.getEmail());
        }

        @Test
        @DisplayName("assigns id = max existing id + 1 when one admin exists")
        void assignsNextIdWhenOneAdminExists() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(5, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            AdminFileManager manager = buildManager();

            manager.addNewAdmin("bob", "pass2", "bob@example.com");

            Admin result = manager.findAdmin("bob");
            assertNotNull(result);
            assertEquals(6, result.getID(), "New ID should be max(5) + 1 = 6");
        }

        @Test
        @DisplayName("assigns id = 2 when only the default admin (id=1) exists")
        void assignsId2WhenOnlyDefaultAdminExists() {
            when(storage.ReadFromFile(ADMIN_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            AdminFileManager manager = buildManager(); // creates default admin id=1

            manager.addNewAdmin("newguy", "secret", "newguy@example.com");

            Admin result = manager.findAdmin("newguy");
            assertNotNull(result);
            assertEquals(2, result.getID());
        }

        @Test
        @DisplayName("assigns increasing ids across multiple sequential additions")
        void assignsIncreasingIdsAcrossMultipleAdditions() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            AdminFileManager manager = buildManager();

            manager.addNewAdmin("bob",     "p2", "bob@example.com");
            manager.addNewAdmin("charlie", "p3", "charlie@example.com");

            assertEquals(2, manager.findAdmin("bob").getID());
            assertEquals(3, manager.findAdmin("charlie").getID());
        }

        @Test
        @DisplayName("writes all admins including new one to file on each add")
        void writesAllAdminsToFileOnAdd() {
            when(storage.ReadFromFile(ADMIN_FILE))
                    .thenReturn(List.of(adminLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(ADMIN_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            AdminFileManager manager = buildManager();

            manager.addNewAdmin("bob", "pass2", "bob@example.com");

            verify(storage).WriteToFile(
                    eq(ADMIN_FILE),
                    argThat(lines ->
                            lines.size() == 2 &&
                                    lines.contains(adminLine(1, "alice", "pass1", "alice@example.com")) &&
                                    lines.contains(adminLine(2, "bob",   "pass2", "bob@example.com"))
                    )
            );
        }
    }
}