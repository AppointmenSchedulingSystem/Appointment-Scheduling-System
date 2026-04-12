package Fall2026.infrastructure.persistence;

import Fall2026.domain.account.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserFileManager.
 *
 * Mock strategy:
 * - CredentialStorage: always mocked — it performs real file I/O.
 * - User: pure domain class, always instantiated directly.
 *
 * ⚠ Requires one source change to be testable:
 *   Add a package-private constructor to UserFileManager that accepts
 *   a CredentialStorage parameter so the mock can be injected:
 *
 *     UserFileManager(CredentialStorage storage) {
 *         this.storage = storage;
 *         loadUsersFromFile();
 *         if (users.isEmpty()) {
 *             createDefaultUser();
 *         }
 *     }
 *
 *   The existing public no-arg constructor is unchanged for production use.
 */
@DisplayName("UserFileManager Tests")
class UserFileManagerTest {

    private static final String USER_FILE = "Users.txt";

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

    /** Builds the 4-field CSV line exactly as UserFileManager writes it. */
    private String userLine(int id, String username, String password, String email) {
        return id + "," + username + "," + password + "," + email;
    }

    /** Creates a UserFileManager with the mock storage injected. */
    private UserFileManager buildManager() {
        return new UserFileManager(storage);
    }

    // =========================================================================
    // Constructor — file has data
    // =========================================================================

    @Nested
    @DisplayName("Constructor — file has data")
    class ConstructorWithDataTests {

        @Test
        @DisplayName("loads a single valid user line from file")
        void loadsSingleUser() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));

            UserFileManager manager = buildManager();

            User found = manager.findUser("alice");
            assertNotNull(found);
            assertEquals(1,                   found.getID());
            assertEquals("alice",             found.getUsername());
            assertEquals("pass1",             found.getPassword());
            assertEquals("alice@example.com", found.getEmail());
        }

        @Test
        @DisplayName("loads multiple valid user lines from file")
        void loadsMultipleUsers() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(List.of(
                    userLine(1, "alice", "pass1", "alice@example.com"),
                    userLine(2, "bob",   "pass2", "bob@example.com")
            ));

            UserFileManager manager = buildManager();

            assertNotNull(manager.findUser("alice"));
            assertNotNull(manager.findUser("bob"));
        }

        @Test
        @DisplayName("does not create default user when file already has users")
        void doesNotWriteDefaultWhenFileHasData() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));

            buildManager();

            // WriteToFile must never be called — no default user was needed
            verify(storage, org.mockito.Mockito.times(0))
                    .WriteToFile(eq(USER_FILE), argThat(lines -> !lines.isEmpty()));
        }
    }

    // =========================================================================
    // Constructor — file is empty (default user path)
    // =========================================================================

    @Nested
    @DisplayName("Constructor — file is empty")
    class ConstructorEmptyFileTests {

        @Test
        @DisplayName("creates default user with id=1 when file is empty")
        void createsDefaultUserWhenFileEmpty() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);

            UserFileManager manager = buildManager();

            User defaultUser = manager.findUser("user");
            assertNotNull(defaultUser);
            assertEquals(1,                  defaultUser.getID());
            assertEquals("user",             defaultUser.getUsername());
            assertEquals("user123",          defaultUser.getPassword());
            assertEquals("user@example.com", defaultUser.getEmail());
        }

        @Test
        @DisplayName("saves default user to file when file is empty")
        void savesDefaultUserToFileWhenEmpty() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);

            buildManager();

            verify(storage).WriteToFile(
                    eq(USER_FILE),
                    argThat(lines ->
                            lines.size() == 1 &&
                                    lines.get(0).equals(userLine(1, "user", "user123", "user@example.com"))
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
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of("alice,pass1,alice@example.com")); // 3 parts

            UserFileManager manager = buildManager();

            // Malformed line skipped → list empty → default user created
            assertNotNull(manager.findUser("user"), "Default user should exist after skipping bad line");
            assertNull(manager.findUser("alice"),   "Malformed user should not have been loaded");
        }

        @Test
        @DisplayName("skips line that has more than 4 comma-separated parts")
        void skipsLineTooManyParts() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of("1,alice,pass1,alice@example.com,extraField"));

            UserFileManager manager = buildManager();

            assertNull(manager.findUser("alice"), "Line with 5 parts should be skipped");
        }

        @Test
        @DisplayName("loads valid lines and skips invalid ones in the same file")
        void loadsValidAndSkipsInvalid() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(List.of(
                    userLine(1, "alice", "pass1", "alice@example.com"), // valid
                    "baddata",                                           // invalid — 1 part
                    userLine(2, "bob",   "pass2", "bob@example.com")    // valid
            ));

            UserFileManager manager = buildManager();

            assertNotNull(manager.findUser("alice"));
            assertNotNull(manager.findUser("bob"));
            assertNull(manager.findUser("baddata"));
        }

        @Test
        @DisplayName("skips line with a non-integer id field")
        void skipsLineWithNonIntegerId() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of("notAnInt,alice,pass1,alice@example.com"));

            UserFileManager manager = buildManager();

            // Parsing throws → line skipped → list empty → default user created
            assertNotNull(manager.findUser("user"), "Default user should exist after parse failure");
            assertNull(manager.findUser("alice"),   "User from bad line should not be loaded");
        }
    }

    // =========================================================================
    // findUser
    // =========================================================================

    @Nested
    @DisplayName("findUser")
    class FindUserTests {

        @Test
        @DisplayName("returns the correct user for an existing username")
        void returnsCorrectUserForExistingUsername() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(List.of(
                    userLine(1, "alice", "pass1", "alice@example.com"),
                    userLine(2, "bob",   "pass2", "bob@example.com")
            ));
            UserFileManager manager = buildManager();

            User result = manager.findUser("bob");

            assertNotNull(result);
            assertEquals(2,               result.getID());
            assertEquals("bob",           result.getUsername());
            assertEquals("bob@example.com", result.getEmail());
        }

        @Test
        @DisplayName("returns null when username does not exist")
        void returnsNullForNonExistentUsername() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));
            UserFileManager manager = buildManager();

            assertNull(manager.findUser("charlie"));
        }

        @Test
        @DisplayName("returns null when searching for a username that was never added")
        void returnsNullForUnknownUsername() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));
            UserFileManager manager = buildManager();

            assertNull(manager.findUser("nobody"));
        }

        @Test
        @DisplayName("username match is case-sensitive")
        void usernameMatchIsCaseSensitive() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "Alice", "pass1", "alice@example.com")));
            UserFileManager manager = buildManager();

            assertNull(manager.findUser("alice"),    "Lowercase 'alice' should not match stored 'Alice'");
            assertNotNull(manager.findUser("Alice"), "Exact-case 'Alice' should match");
        }
    }

    // =========================================================================
    // addNewUser
    // =========================================================================

    @Nested
    @DisplayName("addNewUser")
    class AddNewUserTests {

        @Test
        @DisplayName("adds new user and makes them findable")
        void addsNewUserAndMakesThemFindable() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager();

            manager.addNewUser("bob", "pass2", "bob@example.com");

            User result = manager.findUser("bob");
            assertNotNull(result);
            assertEquals("bob",             result.getUsername());
            assertEquals("pass2",           result.getPassword());
            assertEquals("bob@example.com", result.getEmail());
        }

        @Test
        @DisplayName("assigns id = max existing id + 1 when one user exists")
        void assignsNextIdWhenOneUserExists() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(5, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager();

            manager.addNewUser("bob", "pass2", "bob@example.com");

            assertEquals(6, manager.findUser("bob").getID(),
                    "New ID should be max(5) + 1 = 6");
        }

        @Test
        @DisplayName("assigns id = 2 when only the default user (id=1) exists")
        void assignsId2WhenOnlyDefaultUserExists() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(new ArrayList<>());
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager(); // creates default user id=1

            manager.addNewUser("newguy", "secret", "newguy@example.com");

            assertEquals(2, manager.findUser("newguy").getID());
        }

        @Test
        @DisplayName("assigns increasing ids across multiple sequential additions")
        void assignsIncreasingIdsAcrossMultipleAdditions() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager();

            manager.addNewUser("bob",     "p2", "bob@example.com");
            manager.addNewUser("charlie", "p3", "charlie@example.com");

            assertEquals(2, manager.findUser("bob").getID());
            assertEquals(3, manager.findUser("charlie").getID());
        }

        @Test
        @DisplayName("writes all users including the new one to file on each add")
        void writesAllUsersToFileOnAdd() {
            when(storage.ReadFromFile(USER_FILE))
                    .thenReturn(List.of(userLine(1, "alice", "pass1", "alice@example.com")));
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager();

            manager.addNewUser("bob", "pass2", "bob@example.com");

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(USER_FILE), captor.capture());

            List<String> written = captor.getValue();
            assertEquals(2, written.size());
            assertEquals(userLine(1, "alice", "pass1", "alice@example.com"), written.get(0));
            assertEquals(userLine(2, "bob",   "pass2", "bob@example.com"),   written.get(1));
        }

        @Test
        @DisplayName("boundary: id jumps correctly when existing ids are non-contiguous")
        void assignsCorrectIdForNonContiguousExistingIds() {
            when(storage.ReadFromFile(USER_FILE)).thenReturn(List.of(
                    userLine(1,  "alice", "pass1", "alice@example.com"),
                    userLine(10, "bob",   "pass2", "bob@example.com")
            ));
            when(storage.WriteToFile(eq(USER_FILE), argThat(l -> !l.isEmpty()))).thenReturn(true);
            UserFileManager manager = buildManager();

            manager.addNewUser("charlie", "pass3", "charlie@example.com");

            assertEquals(11, manager.findUser("charlie").getID(),
                    "New ID should be max(10) + 1 = 11");
        }
    }
}