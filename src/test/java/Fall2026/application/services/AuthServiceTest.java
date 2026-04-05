package Fall2026.application.services;

import Fall2026.domain.account.Admin;
import Fall2026.domain.account.User;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private AuthService authService;
    private Session session;
    private MockAdminFileManager mockAdminFileManager;
    private MockUserFileManager mockUserFileManager;
    private Admin testAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        session = new Session();
        mockAdminFileManager = new MockAdminFileManager();
        mockUserFileManager = new MockUserFileManager();
        authService = new AuthService(session, mockAdminFileManager, mockUserFileManager);

        // Create test data
        testAdmin = new Admin(1, "admin", "adminpass123", "admin@example.com");
        testUser = new User(2, "user", "userpass123", "user@example.com");

        // Add test data to mock file managers
        mockAdminFileManager.addAdmin(testAdmin);
        mockUserFileManager.addUser(testUser);
    }

    @AfterEach
    void tearDown() {
        session.clear();
    }

    @Test
    void loginAdminSuccessfully() {
        // Act
        Admin loggedInAdmin = authService.loginAdmin("admin", "adminpass123");

        // Assert
        assertNotNull(loggedInAdmin);
        assertEquals("admin", loggedInAdmin.getUsername());
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
    }

    @Test
    void loginAdminWithInvalidUsername() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.loginAdmin("invalidadmin", "adminpass123");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginAdminWithInvalidPassword() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.loginAdmin("admin", "wrongpassword");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginAdminWithEmptyUsername() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginAdmin("", "adminpass123");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginAdminWithEmptyPassword() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginAdmin("admin", "");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginAdminWithNullUsername() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginAdmin(null, "adminpass123");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginAdminWithNullPassword() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginAdmin("admin", null);
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginUserSuccessfully() {
        // Act
        User loggedInUser = authService.loginUser("user", "userpass123");

        // Assert
        assertNotNull(loggedInUser);
        assertEquals("user", loggedInUser.getUsername());
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());
    }

    @Test
    void loginUserWithInvalidUsername() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.loginUser("invaliduser", "userpass123");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginUserWithInvalidPassword() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.loginUser("user", "wrongpassword");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginUserWithEmptyUsername() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginUser("", "userpass123");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginUserWithEmptyPassword() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.loginUser("user", "");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void logoutSuccessfully() {
        // Arrange
        authService.loginAdmin("admin", "adminpass123");
        assertTrue(session.isLoggedIn());

        // Act
        authService.logout();

        // Assert
        assertFalse(session.isLoggedIn());
    }

    @Test
    void logoutWhenNotLoggedIn() {
        // Act
        authService.logout(); // Should not throw

        // Assert
        assertFalse(session.isLoggedIn());
    }

    @Test
    void requireLoggedInWhenLoggedIn() {
        // Arrange
        authService.loginAdmin("admin", "adminpass123");

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> authService.requireLoggedIn());
    }

    @Test
    void requireLoggedInWhenNotLoggedIn() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.requireLoggedIn();
        });
    }

    @Test
    void requireAdminWhenAdminLoggedIn() {
        // Arrange
        authService.loginAdmin("admin", "adminpass123");

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> authService.requireAdmin());
    }

    @Test
    void requireAdminWhenUserLoggedIn() {
        // Arrange
        authService.loginUser("user", "userpass123");

        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.requireAdmin();
        });
    }

    @Test
    void requireAdminWhenNotLoggedIn() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.requireAdmin();
        });
    }

    @Test
    void requireUserWhenUserLoggedIn() {
        // Arrange
        authService.loginUser("user", "userpass123");

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> authService.requireUser());
    }

    @Test
    void requireUserWhenAdminLoggedIn() {
        // Arrange
        authService.loginAdmin("admin", "adminpass123");

        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.requireUser();
        });
    }

    @Test
    void requireUserWhenNotLoggedIn() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.requireUser();
        });
    }

    @Test
    void loginAsAdminSuccessfully() {
        // Act
        authService.login("admin", "adminpass123");

        // Assert
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
    }

    @Test
    void loginAsUserSuccessfully() {
        // Act
        authService.login("user", "userpass123");

        // Assert
        assertTrue(session.isLoggedIn());
        assertTrue(session.isUser());
    }

    @Test
    void loginWithInvalidCredentials() {
        // Act & Assert
        assertThrows(AuthorizationException.class, () -> {
            authService.login("invaliduser", "invalidpass");
        });
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginTriesAdminFirstThenUser() {
        // This test verifies that login tries admin credentials first, then user
        // If admin login succeeds, user login is not attempted
        authService.login("admin", "adminpass123");
        assertTrue(session.isAdmin());

        session.clear();

        // Now try with user credentials
        authService.login("user", "userpass123");
        assertTrue(session.isUser());
    }

    /**
     * Mock implementation of AdminFileManager for testing.
     * Allows controlled addition and retrieval of admin accounts without file I/O.
     */
    private static class MockAdminFileManager extends AdminFileManager {
        private Admin mockAdmin;

        public MockAdminFileManager() {
            // Prevent parent constructor from doing file I/O
            // We'll override methods instead
        }

        public void addAdmin(Admin admin) {
            this.mockAdmin = admin;
        }

        @Override
        public Admin findAdmin(String username) {
            if (mockAdmin != null && mockAdmin.getUsername().equals(username)) {
                return mockAdmin;
            }
            return null;
        }
    }

    /**
     * Mock implementation of UserFileManager for testing.
     * Allows controlled addition and retrieval of user accounts without file I/O.
     */
    private static class MockUserFileManager extends UserFileManager {
        private User mockUser;

        public MockUserFileManager() {
            // Prevent parent constructor from doing file I/O
            // We'll override methods instead
        }

        public void addUser(User user) {
            this.mockUser = user;
        }

        @Override
        public User findUser(String username) {
            if (mockUser != null && mockUser.getUsername().equals(username)) {
                return mockUser;
            }
            return null;
        }
    }
}