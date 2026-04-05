package Fall2026.application.services;

import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {
    private Session session;
    private Admin testAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        session = new Session();
        testAdmin = new Admin(1, "admin", "password123", "admin@example.com");
        testUser = new User(2, "user", "password123", "user@example.com");
    }

    @AfterEach
    void tearDown() {
        session.clear();
    }

    // ========== getCurrentAccount() Tests ==========

    @Test
    void getCurrentAccountReturnsNullWhenNoAccountSet() {
        // Act
        Role currentAccount = session.getCurrentAccount();

        // Assert
        assertNull(currentAccount);
    }

    @Test
    void getCurrentAccountReturnsAdminAfterSettingAdmin() {
        // Arrange
        session.setCurrentAccount(testAdmin);

        // Act
        Role currentAccount = session.getCurrentAccount();

        // Assert
        assertNotNull(currentAccount);
        assertEquals(testAdmin, currentAccount);
        assertSame(testAdmin, currentAccount);
    }

    @Test
    void getCurrentAccountReturnsUserAfterSettingUser() {
        // Arrange
        session.setCurrentAccount(testUser);

        // Act
        Role currentAccount = session.getCurrentAccount();

        // Assert
        assertNotNull(currentAccount);
        assertEquals(testUser, currentAccount);
        assertSame(testUser, currentAccount);
    }

    @Test
    void getCurrentAccountReturnsLatestAccountAfterMultipleSets() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        session.setCurrentAccount(testUser);

        // Act
        Role currentAccount = session.getCurrentAccount();

        // Assert
        assertEquals(testUser, currentAccount);
        assertNotEquals(testAdmin, currentAccount);
    }

    // ========== setCurrentAccount() Tests ==========

    @Test
    void setCurrentAccountWithAdmin() {
        // Act
        session.setCurrentAccount(testAdmin);

        // Assert
        assertEquals(testAdmin, session.getCurrentAccount());
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
    }

    @Test
    void setCurrentAccountWithUser() {
        // Act
        session.setCurrentAccount(testUser);

        // Assert
        assertEquals(testUser, session.getCurrentAccount());
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());
    }

    @Test
    void setCurrentAccountWithNull() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isLoggedIn());

        // Act
        session.setCurrentAccount(null);

        // Assert
        assertNull(session.getCurrentAccount());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void setCurrentAccountCanReplaceExistingAccount() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isAdmin());

        // Act
        session.setCurrentAccount(testUser);

        // Assert
        assertEquals(testUser, session.getCurrentAccount());
        assertTrue(session.isUser());
        assertFalse(session.isAdmin());
    }

    @Test
    void setCurrentAccountMultipleTimes() {
        // Arrange & Act
        session.setCurrentAccount(testAdmin);
        session.setCurrentAccount(testUser);
        session.setCurrentAccount(testAdmin);

        // Assert
        assertEquals(testAdmin, session.getCurrentAccount());
        assertTrue(session.isAdmin());
        assertFalse(session.isUser());
    }

    // ========== clear() Tests ==========

    @Test
    void clearRemovesCurrentAccount() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isLoggedIn());

        // Act
        session.clear();

        // Assert
        assertNull(session.getCurrentAccount());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void clearMakesNotAdminAndNotUser() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isAdmin());

        // Act
        session.clear();

        // Assert
        assertFalse(session.isAdmin());
        assertFalse(session.isUser());
    }

    @Test
    void clearCanBeCalledMultipleTimes() {
        // Arrange
        session.setCurrentAccount(testAdmin);

        // Act
        session.clear();
        session.clear();
        session.clear();

        // Assert
        assertNull(session.getCurrentAccount());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void clearOnEmptySessionDoesNotThrow() {
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> {
            session.clear();
        });
        assertNull(session.getCurrentAccount());
    }

    // ========== isLoggedIn() Tests ==========

    @Test
    void isLoggedInReturnsFalseByDefault() {
        // Act
        boolean loggedIn = session.isLoggedIn();

        // Assert
        assertFalse(loggedIn);
    }

    @Test
    void isLoggedInReturnsTrueAfterSettingAdmin() {
        // Arrange
        session.setCurrentAccount(testAdmin);

        // Act
        boolean loggedIn = session.isLoggedIn();

        // Assert
        assertTrue(loggedIn);
    }

    @Test
    void isLoggedInReturnsTrueAfterSettingUser() {
        // Arrange
        session.setCurrentAccount(testUser);

        // Act
        boolean loggedIn = session.isLoggedIn();

        // Assert
        assertTrue(loggedIn);
    }

    @Test
    void isLoggedInReturnsFalseAfterClear() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isLoggedIn());

        // Act
        session.clear();
        boolean loggedIn = session.isLoggedIn();

        // Assert
        assertFalse(loggedIn);
    }

    @Test
    void isLoggedInReturnsFalseAfterSettingNull() {
        // Arrange
        session.setCurrentAccount(testUser);
        assertTrue(session.isLoggedIn());

        // Act
        session.setCurrentAccount(null);
        boolean loggedIn = session.isLoggedIn();

        // Assert
        assertFalse(loggedIn);
    }

    // ========== isAdmin() Tests ==========

    @Test
    void isAdminReturnsFalseByDefault() {
        // Act
        boolean isAdmin = session.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void isAdminReturnsTrueWhenAdminLoggedIn() {
        // Arrange
        session.setCurrentAccount(testAdmin);

        // Act
        boolean isAdmin = session.isAdmin();

        // Assert
        assertTrue(isAdmin);
    }

    @Test
    void isAdminReturnsFalseWhenUserLoggedIn() {
        // Arrange
        session.setCurrentAccount(testUser);

        // Act
        boolean isAdmin = session.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void isAdminReturnsFalseAfterClearingSession() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isAdmin());

        // Act
        session.clear();
        boolean isAdmin = session.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void isAdminChangesAfterSwitchingAccounts() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isAdmin());

        // Act
        session.setCurrentAccount(testUser);
        boolean isAdmin = session.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    @Test
    void isAdminReturnsFalseAfterSettingNull() {
        // Arrange
        session.setCurrentAccount(testAdmin);
        assertTrue(session.isAdmin());

        // Act
        session.setCurrentAccount(null);
        boolean isAdmin = session.isAdmin();

        // Assert
        assertFalse(isAdmin);
    }

    // ========== isUser() Tests ==========

    @Test
    void isUserReturnsFalseByDefault() {
        // Act
        boolean isUser = session.isUser();

        // Assert
        assertFalse(isUser);
    }

    @Test
    void isUserReturnsTrueWhenUserLoggedIn() {
        // Arrange
        session.setCurrentAccount(testUser);

        // Act
        boolean isUser = session.isUser();

        // Assert
        assertTrue(isUser);
    }

    @Test
    void isUserReturnsFalseWhenAdminLoggedIn() {
        // Arrange
        session.setCurrentAccount(testAdmin);

        // Act
        boolean isUser = session.isUser();

        // Assert
        assertFalse(isUser);
    }

    @Test
    void isUserReturnsFalseAfterClearingSession() {
        // Arrange
        session.setCurrentAccount(testUser);
        assertTrue(session.isUser());

        // Act
        session.clear();
        boolean isUser = session.isUser();

        // Assert
        assertFalse(isUser);
    }

    @Test
    void isUserChangesAfterSwitchingAccounts() {
        // Arrange
        session.setCurrentAccount(testUser);
        assertTrue(session.isUser());

        // Act
        session.setCurrentAccount(testAdmin);
        boolean isUser = session.isUser();

        // Assert
        assertFalse(isUser);
    }

    @Test
    void isUserReturnsFalseAfterSettingNull() {
        // Arrange
        session.setCurrentAccount(testUser);
        assertTrue(session.isUser());

        // Act
        session.setCurrentAccount(null);
        boolean isUser = session.isUser();

        // Assert
        assertFalse(isUser);
    }

    // ========== State Management Tests ==========

    @Test
    void adminAndUserAreNeverBothTrue() {
        // Arrange & Act
        session.setCurrentAccount(testAdmin);

        // Assert
        assertTrue(session.isAdmin());
        assertFalse(session.isUser());
        assertFalse(session.isAdmin() && session.isUser()); // Both should never be true

        // Act again
        session.setCurrentAccount(testUser);

        // Assert
        assertFalse(session.isAdmin());
        assertTrue(session.isUser());
        assertFalse(session.isAdmin() && session.isUser()); // Both should never be true
    }

    @Test
    void isLoggedInCorrelatesWithCurrentAccount() {
        // Arrange & Act
        session.setCurrentAccount(testAdmin);

        // Assert
        assertEquals(session.isLoggedIn(), session.getCurrentAccount() != null);

        // Act again
        session.clear();

        // Assert
        assertEquals(session.isLoggedIn(), session.getCurrentAccount() != null);

        // Act again
        session.setCurrentAccount(testUser);

        // Assert
        assertEquals(session.isLoggedIn(), session.getCurrentAccount() != null);
    }

    @Test
    void sequenceOfOperations() {
        // Arrange & Act - Test complex sequence of operations
        assertFalse(session.isLoggedIn());

        session.setCurrentAccount(testUser);
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());
        assertFalse(session.isAdmin());

        session.setCurrentAccount(testAdmin);
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
        assertFalse(session.isUser());

        session.clear();
        assertFalse(session.isLoggedIn());
        assertFalse(session.isAdmin());
        assertFalse(session.isUser());

        session.setCurrentAccount(testUser);
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());

        // Assert all values at the end
        assertEquals(testUser, session.getCurrentAccount());
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());
        assertFalse(session.isAdmin());
    }
}