package Fall2026.application.services;

import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Session class.
 * Tests session management, account switching, and authorization state tracking.
 *
 * Test Coverage:
 * - Account state management (get, set, clear)
 * - Authorization checks (isAdmin, isUser, isLoggedIn)
 * - State transitions (switching accounts, logging out)
 * - Invariants (mutual exclusivity, consistency)
 * - Edge cases (null handling, rapid transitions)
 * - Stress tests (multiple operations)
 */
@DisplayName("Session Management Tests")
class SessionTest {
    private Session session;
    private Admin testAdmin;
    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        session = new Session();
        testAdmin = createTestAdmin(1, "admin");
        testUser = createTestUser(2, "user");
        testUser2 = createTestUser(3, "user2");
    }

    @AfterEach
    void tearDown() {
        session.clear();
    }

    // ========== Test Data Factories ==========

    /**
     * Factory: Create a test Admin instance
     */
    private Admin createTestAdmin(int id, String name) {
        return new Admin(id, name, "password123", name + "@example.com");
    }

    /**
     * Factory: Create a test User instance
     */
    private User createTestUser(int id, String name) {
        return new User(id, name, "password123", name + "@example.com");
    }

    // ========== getCurrentAccount() Tests ==========

    @Nested
    @DisplayName("getCurrentAccount() Tests")
    class GetCurrentAccountTests {

        @Test
        @DisplayName("should return null when no account is set")
        void returnsNullWhenNoAccountSet() {
            assertNull(session.getCurrentAccount(),
                "Current account should be null before any account is set");
        }

        @Test
        @DisplayName("should return the exact Admin instance that was set")
        void returnsAdminAfterSettingAdmin() {
            session.setCurrentAccount(testAdmin);

            Role currentAccount = session.getCurrentAccount();

            assertNotNull(currentAccount);
            assertSame(testAdmin, currentAccount, "Should return the same Admin instance");
            assertEquals(testAdmin, currentAccount);
        }

        @Test
        @DisplayName("should return the exact User instance that was set")
        void returnsUserAfterSettingUser() {
            session.setCurrentAccount(testUser);

            Role currentAccount = session.getCurrentAccount();

            assertNotNull(currentAccount);
            assertSame(testUser, currentAccount, "Should return the same User instance");
            assertEquals(testUser, currentAccount);
        }

        @Test
        @DisplayName("should return the latest account after multiple sets")
        void returnsLatestAccountAfterMultipleSets() {
            session.setCurrentAccount(testAdmin);
            session.setCurrentAccount(testUser);

            Role currentAccount = session.getCurrentAccount();

            assertEquals(testUser, currentAccount, "Should return the most recently set account");
            assertNotEquals(testAdmin, currentAccount);
        }

        @Test
        @DisplayName("should preserve account through multiple intermediate operations")
        void preservesAccountThroughOperations() {
            session.setCurrentAccount(testAdmin);

            // Call multiple methods to ensure state is preserved
            boolean isAdmin1 = session.isAdmin();
            boolean isLoggedIn1 = session.isLoggedIn();
            Role account1 = session.getCurrentAccount();
            boolean isAdmin2 = session.isAdmin();

            assertEquals(testAdmin, account1);
            assertTrue(isAdmin1);
            assertTrue(isLoggedIn1);
            assertTrue(isAdmin2);
        }

        @Test
        @DisplayName("should return different instances for different users with same properties")
        void distinguishesDifferentUserInstances() {
            User differentUser = createTestUser(2, "user");
            session.setCurrentAccount(differentUser);

            Role currentAccount = session.getCurrentAccount();

            // Should be the exact instance, not just equal
            assertSame(differentUser, currentAccount);
            assertNotSame(testUser, currentAccount);
        }
    }

    // ========== setCurrentAccount() Tests ==========

    @Nested
    @DisplayName("setCurrentAccount() Tests")
    class SetCurrentAccountTests {

        @Test
        @DisplayName("should set Admin and update all related states")
        void setCurrentAccountWithAdmin() {
            session.setCurrentAccount(testAdmin);

            assertEquals(testAdmin, session.getCurrentAccount());
            assertTrue(session.isLoggedIn());
            assertTrue(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should set User and update all related states")
        void setCurrentAccountWithUser() {
            session.setCurrentAccount(testUser);

            assertEquals(testUser, session.getCurrentAccount());
            assertTrue(session.isLoggedIn());
            assertTrue(session.isUser());
            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should handle setting to null after having an account")
        void setCurrentAccountWithNull() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isLoggedIn());

            session.setCurrentAccount(null);

            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());
            assertFalse(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should replace existing Admin with User")
        void replaceAdminWithUser() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isAdmin());

            session.setCurrentAccount(testUser);

            assertEquals(testUser, session.getCurrentAccount());
            assertTrue(session.isUser());
            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should replace existing User with Admin")
        void replaceUserWithAdmin() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isUser());

            session.setCurrentAccount(testAdmin);

            assertEquals(testAdmin, session.getCurrentAccount());
            assertTrue(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should handle rapid successive changes")
        void multipleSuccessiveChanges() {
            session.setCurrentAccount(testAdmin);
            session.setCurrentAccount(testUser);
            session.setCurrentAccount(testAdmin);

            assertEquals(testAdmin, session.getCurrentAccount());
            assertTrue(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should handle alternating between same account types")
        void alternatingBetweenDifferentUsers() {
            User user2 = new User(3, "user2", "pass456", "user2@example.com");

            session.setCurrentAccount(testUser);
            assertEquals(testUser, session.getCurrentAccount());

            session.setCurrentAccount(user2);
            assertEquals(user2, session.getCurrentAccount());
            assertNotEquals(testUser, session.getCurrentAccount());
        }
    }

    // ========== clear() Tests ==========

    @Nested
    @DisplayName("clear() Tests")
    class ClearTests {

        @Test
        @DisplayName("should clear Admin account completely")
        void clearRemovesAdminAccount() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isLoggedIn());

            session.clear();

            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());
            assertFalse(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should clear User account completely")
        void clearRemovesUserAccount() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isLoggedIn());

            session.clear();

            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());
            assertFalse(session.isUser());
            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should be idempotent - calling multiple times is safe")
        void clearCanBeCalledMultipleTimes() {
            session.setCurrentAccount(testAdmin);

            session.clear();
            session.clear();
            session.clear();

            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());
        }

        @Test
        @DisplayName("should not throw exception when called on empty session")
        void clearOnEmptySessionDoesNotThrow() {
            assertDoesNotThrow(() -> {
                session.clear();
                session.clear();
            });
            assertNull(session.getCurrentAccount());
        }

        @Test
        @DisplayName("should restore session to initial state")
        void clearRestoresInitialState() {
            // Record initial state
            boolean initialLoggedIn = session.isLoggedIn();
            Role initialAccount = session.getCurrentAccount();

            // Set and then clear
            session.setCurrentAccount(testAdmin);
            session.clear();

            // Verify restored to initial
            assertEquals(initialLoggedIn, session.isLoggedIn());
            assertEquals(initialAccount, session.getCurrentAccount());
        }
    }

    // ========== isLoggedIn() Tests ==========

    @Nested
    @DisplayName("isLoggedIn() Tests")
    class IsLoggedInTests {

        @Test
        @DisplayName("should return false by default")
        void isLoggedInReturnsFalseByDefault() {
            assertFalse(session.isLoggedIn());
        }

        @Test
        @DisplayName("should return true after setting Admin")
        void isLoggedInReturnsTrueAfterSettingAdmin() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isLoggedIn());
        }

        @Test
        @DisplayName("should return true after setting User")
        void isLoggedInReturnsTrueAfterSettingUser() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isLoggedIn());
        }

        @Test
        @DisplayName("should return false after clear")
        void isLoggedInReturnsFalseAfterClear() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isLoggedIn());

            session.clear();

            assertFalse(session.isLoggedIn());
        }

        @Test
        @DisplayName("should return false after setting to null")
        void isLoggedInReturnsFalseAfterSettingNull() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isLoggedIn());

            session.setCurrentAccount(null);

            assertFalse(session.isLoggedIn());
        }

        @Test
        @DisplayName("should correlate exactly with currentAccount not null")
        void correlatesWithCurrentAccount() {
            // Empty session
            assertEquals(null != session.getCurrentAccount(), session.isLoggedIn());

            // Set Admin
            session.setCurrentAccount(testAdmin);
            assertEquals(null != session.getCurrentAccount(), session.isLoggedIn());

            // Set User
            session.setCurrentAccount(testUser);
            assertEquals(null != session.getCurrentAccount(), session.isLoggedIn());

            // Clear
            session.clear();
            assertEquals(null != session.getCurrentAccount(), session.isLoggedIn());
        }
    }

    // ========== isAdmin() Tests ==========

    @Nested
    @DisplayName("isAdmin() Tests")
    class IsAdminTests {

        @Test
        @DisplayName("should return false by default")
        void isAdminReturnsFalseByDefault() {
            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should return true only when Admin is logged in")
        void isAdminReturnsTrueWhenAdminLoggedIn() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isAdmin());
        }

        @Test
        @DisplayName("should return false when User is logged in")
        void isAdminReturnsFalseWhenUserLoggedIn() {
            session.setCurrentAccount(testUser);
            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should return false after clearing Admin session")
        void isAdminReturnsFalseAfterClearingSession() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isAdmin());

            session.clear();

            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should change when switching from Admin to User")
        void isAdminChangesAfterSwitchingAccounts() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isAdmin());

            session.setCurrentAccount(testUser);

            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should return false after setting null")
        void isAdminReturnsFalseAfterSettingNull() {
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isAdmin());

            session.setCurrentAccount(null);

            assertFalse(session.isAdmin());
        }

        @Test
        @DisplayName("should be consistent with instanceof check")
        void consistentWithInstanceof() {
            session.setCurrentAccount(testAdmin);
            assertEquals(session.getCurrentAccount() instanceof Admin, session.isAdmin());

            session.setCurrentAccount(testUser);
            assertEquals(session.getCurrentAccount() instanceof Admin, session.isAdmin());
        }
    }

    // ========== isUser() Tests ==========

    @Nested
    @DisplayName("isUser() Tests")
    class IsUserTests {

        @Test
        @DisplayName("should return false by default")
        void isUserReturnsFalseByDefault() {
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should return true only when User is logged in")
        void isUserReturnsTrueWhenUserLoggedIn() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isUser());
        }

        @Test
        @DisplayName("should return false when Admin is logged in")
        void isUserReturnsFalseWhenAdminLoggedIn() {
            session.setCurrentAccount(testAdmin);
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should return false after clearing User session")
        void isUserReturnsFalseAfterClearingSession() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isUser());

            session.clear();

            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should change when switching from User to Admin")
        void isUserChangesAfterSwitchingAccounts() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isUser());

            session.setCurrentAccount(testAdmin);

            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should return false after setting null")
        void isUserReturnsFalseAfterSettingNull() {
            session.setCurrentAccount(testUser);
            assertTrue(session.isUser());

            session.setCurrentAccount(null);

            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("should be consistent with instanceof check")
        void consistentWithInstanceof() {
            session.setCurrentAccount(testUser);
            assertEquals(session.getCurrentAccount() instanceof User, session.isUser());

            session.setCurrentAccount(testAdmin);
            assertEquals(session.getCurrentAccount() instanceof User, session.isUser());
        }
    }

    // ========== State Management & Invariant Tests ==========

    @Nested
    @DisplayName("State Management & Invariant Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Admin and User should never both be true simultaneously")
        void adminAndUserAreNeverBothTrue() {
            // Test with Admin
            session.setCurrentAccount(testAdmin);
            assertFalse(session.isAdmin() && session.isUser(),
                "Admin and User should not both be true");

            // Test with User
            session.setCurrentAccount(testUser);
            assertFalse(session.isAdmin() && session.isUser(),
                "Admin and User should not both be true");

            // Test with null
            session.clear();
            assertFalse(session.isAdmin() && session.isUser(),
                "Neither should be true when logged out");
        }

        @Test
        @DisplayName("isLoggedIn() should equal (currentAccount != null)")
        void isLoggedInCorrelatesWithCurrentAccount() {
            // Test empty
            assertEquals(session.getCurrentAccount() != null, session.isLoggedIn());

            // Test with Admin
            session.setCurrentAccount(testAdmin);
            assertEquals(session.getCurrentAccount() != null, session.isLoggedIn());

            // Test with User
            session.setCurrentAccount(testUser);
            assertEquals(session.getCurrentAccount() != null, session.isLoggedIn());

            // Test after clear
            session.clear();
            assertEquals(session.getCurrentAccount() != null, session.isLoggedIn());
        }

        @Test
        @DisplayName("isAdmin() should equal (currentAccount instanceof Admin)")
        void isAdminCorrelatesWithInstanceCheck() {
            session.setCurrentAccount(testAdmin);
            assertEquals(session.getCurrentAccount() instanceof Admin, session.isAdmin());

            session.setCurrentAccount(testUser);
            assertEquals(session.getCurrentAccount() instanceof Admin, session.isAdmin());

            session.clear();
            assertEquals(session.getCurrentAccount() instanceof Admin, session.isAdmin());
        }

        @Test
        @DisplayName("isUser() should equal (currentAccount instanceof User)")
        void isUserCorrelatesWithInstanceCheck() {
            session.setCurrentAccount(testUser);
            assertEquals(session.getCurrentAccount() instanceof User, session.isUser());

            session.setCurrentAccount(testAdmin);
            assertEquals(session.getCurrentAccount() instanceof User, session.isUser());

            session.clear();
            assertEquals(session.getCurrentAccount() instanceof User, session.isUser());
        }

        @Test
        @DisplayName("complete workflow: login, switch, logout, verify clean state")
        void completeWorkflow() {
            // Initial state
            assertFalse(session.isLoggedIn());
            assertNull(session.getCurrentAccount());

            // User login
            session.setCurrentAccount(testUser);
            assertTrue(session.isLoggedIn());
            assertTrue(session.isUser());
            assertFalse(session.isAdmin());
            assertEquals(testUser, session.getCurrentAccount());

            // Admin login (replaces user)
            session.setCurrentAccount(testAdmin);
            assertTrue(session.isLoggedIn());
            assertTrue(session.isAdmin());
            assertFalse(session.isUser());
            assertEquals(testAdmin, session.getCurrentAccount());

            // Logout
            session.clear();
            assertFalse(session.isLoggedIn());
            assertFalse(session.isAdmin());
            assertFalse(session.isUser());
            assertNull(session.getCurrentAccount());
        }

        @Test
        @DisplayName("rapid state transitions maintain consistency")
        void rapidStateTransitions() {
            for (int i = 0; i < 5; i++) {
                session.setCurrentAccount(testAdmin);
                assertTrue(session.isAdmin());
                assertFalse(session.isUser());

                session.setCurrentAccount(testUser);
                assertFalse(session.isAdmin());
                assertTrue(session.isUser());
            }

            session.clear();
            assertFalse(session.isAdmin());
            assertFalse(session.isUser());
        }

        @Test
        @DisplayName("session maintains isolation - state only reflects current account")
        void sessionIsolation() {
            User user2 = new User(99, "other", "pass999", "other@example.com");

            session.setCurrentAccount(testUser);
            assertEquals(testUser, session.getCurrentAccount());

            // Create but don't set user2 - session should not be affected
            session.setCurrentAccount(user2);
            assertEquals(user2, session.getCurrentAccount());
            assertNotEquals(testUser, session.getCurrentAccount());
        }
    }

    // ========== Parameterized & Stress Tests ==========

    @Nested
    @DisplayName("Parameterized & Stress Tests")
    class ParameterizedAndStressTests {

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10, 50, 100 })
        @DisplayName("should handle rapid state transitions with consistency")
        void stressTestRapidTransitions(int iterations) {
            for (int i = 0; i < iterations; i++) {
                // Admin transition
                session.setCurrentAccount(testAdmin);
                assertTrue(session.isAdmin(), "Admin should be true at iteration " + i);
                assertTrue(session.isLoggedIn(), "Should be logged in at iteration " + i);
                assertFalse(session.isUser(), "User should be false at iteration " + i);

                // User transition
                session.setCurrentAccount(testUser);
                assertFalse(session.isAdmin(), "Admin should be false at iteration " + i);
                assertTrue(session.isLoggedIn(), "Should be logged in at iteration " + i);
                assertTrue(session.isUser(), "User should be true at iteration " + i);
            }
        }

        @Test
        @DisplayName("should handle setting same account multiple times")
        void multipleSetSameAccount() {
            for (int i = 0; i < 10; i++) {
                session.setCurrentAccount(testAdmin);
                assertEquals(testAdmin, session.getCurrentAccount());
                assertTrue(session.isAdmin());
            }
        }

        @Test
        @DisplayName("should handle alternating between two users efficiently")
        void alternatingBetweenMultipleUsers() {
            for (int i = 0; i < 20; i++) {
                if (i % 2 == 0) {
                    session.setCurrentAccount(testUser);
                    assertTrue(session.isUser());
                    assertFalse(session.isAdmin());
                } else {
                    session.setCurrentAccount(testUser2);
                    assertTrue(session.isUser());
                    assertFalse(session.isAdmin());
                    assertNotEquals(testUser, session.getCurrentAccount());
                }
            }
        }

        @Test
        @DisplayName("should maintain performance with repeated operations")
        void performanceUnderLoad() {
            long startTime = System.nanoTime();

            for (int i = 0; i < 1000; i++) {
                session.setCurrentAccount(testAdmin);
                session.isAdmin();
                session.isLoggedIn();
                session.setCurrentAccount(testUser);
                session.isUser();
                session.clear();
            }

            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            // Should complete 1000 iterations in reasonable time (< 100ms)
            assertTrue(durationMs < 100, "Operations took " + durationMs + "ms, should be faster");
        }
    }

    // ========== Boundary & Edge Case Tests ==========

    @Nested
    @DisplayName("Boundary & Edge Case Tests")
    class BoundaryAndEdgeCaseTests {

        @Test
        @DisplayName("should handle admin with null email gracefully")
        void adminWithNullEmail() {
            Admin adminNoEmail = new Admin(99, "noemail", "pass", null);
            session.setCurrentAccount(adminNoEmail);

            assertEquals(adminNoEmail, session.getCurrentAccount());
            assertTrue(session.isAdmin());
        }

        @Test
        @DisplayName("should handle user with special characters in name")
        void userWithSpecialCharactersInName() {
            User specialUser = new User(100, "user-_@123", "pass", "user@example.com");
            session.setCurrentAccount(specialUser);

            assertEquals(specialUser, session.getCurrentAccount());
            assertTrue(session.isUser());
        }

        @Test
        @DisplayName("should handle rapid clear operations")
        void rapidClearOperations() {
            session.setCurrentAccount(testAdmin);
            session.clear();
            session.clear();
            session.clear();

            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());
        }

        @Test
        @DisplayName("should handle null check followed by operations")
        void nullCheckFollowedByOperations() {
            assertNull(session.getCurrentAccount());
            assertFalse(session.isLoggedIn());

            session.setCurrentAccount(testUser);
            assertNotNull(session.getCurrentAccount());
            assertTrue(session.isLoggedIn());
        }

        @Test
        @DisplayName("should preserve state across multiple assertion calls")
        void statePreservationAcrossAssertions() {
            session.setCurrentAccount(testAdmin);

            // Multiple assertions shouldn't change state
            for (int i = 0; i < 5; i++) {
                assertEquals(testAdmin, session.getCurrentAccount());
                assertTrue(session.isAdmin());
                assertTrue(session.isLoggedIn());
            }
        }
    }
}