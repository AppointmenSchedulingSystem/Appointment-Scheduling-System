package Fall2026.application.services;

import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    //mocking
    private AdminFileManager adminFileManager;
    private UserFileManager userFileManager;
    private Session session;
    //real class
    private AuthService authService;


    @BeforeEach
    void setUp() {
        adminFileManager = mock(AdminFileManager.class);
        userFileManager = mock(UserFileManager.class);
        session = mock(Session.class);
        // Inject the mocks into the real service (constructor injection)
        authService = new AuthService(session, adminFileManager, userFileManager);
    }

    @TempDir
    Path tempDir;

    private void writeTempUsersFile(String... lines) throws IOException {
        Path usersFile = tempDir.resolve("users.txt");
        Files.write(usersFile, java.util.List.of(lines));
        System.setProperty("user.dir", tempDir.toString());
    }




        //======================================================
        // Admin Login Tests
        //======================================================
    @Test
    void loginAdmin() {
        //Arrange
        Admin fackeAdmin = new Admin(1,"admin1","admin123", "admin@example.com");
        when(adminFileManager.findAdmin("admin1")).thenReturn(fackeAdmin);
        //Act
        Admin result = authService.loginAdmin("admin1", "admin123");
        //Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("admin1", result.getUsername(), "Username should match");

        verify(session).setCurrentAccount(fackeAdmin);
    }

    @Test
    void loginAdmin_InvalidCredentials() {
        //Arrange
        when(adminFileManager.findAdmin("admin1")).thenReturn(null);
        //Act & Assert
        assertThrows(RuntimeException.class, () -> authService.loginAdmin("admin1", "wrongpassword"), "Should throw exception for invalid credentials");
    }

    @Test
    void loginAdmin_adminNotFound() {
        // Arrange — mock returns null (admin doesn't exist)
        when(adminFileManager.findAdmin("ghost")).thenReturn(null);

        // assertThrows checks that the method throws the right exception
        assertThrows(AuthorizationException.class,
                () -> authService.loginAdmin("ghost", "anyPassword"));
    }
    @Test
    void loginAdmin_shouldThrowAuthorizationException_whenPasswordIsWrong() {
        Admin fakeAdmin = new Admin(1, "ali", "correctPassword", "ali@example.com");
        when(adminFileManager.findAdmin("ali")).thenReturn(fakeAdmin);

        assertThrows(AuthorizationException.class,
                () -> authService.loginAdmin("ali", "wrongPassword"));
    }

    @Test
    void loginAdmin_shouldThrowValidationException_whenUsernameIsBlank() {
        // validateLoginInput() fires before any file lookup
        assertThrows(ValidationException.class,
                () -> authService.loginAdmin("", "pass123"));
    }

    @Test
    void loginAdmin_shouldThrowValidationException_whenPasswordIsNull() {
        assertThrows(ValidationException.class,
                () -> authService.loginAdmin("ali", null));
    }

    //======================================================
    //Logout
    //======================================================

    @Test
    void logout_shouldClearSession() {
    authService.logout();
    verify(session,times(1)).clear();
    }


    //======================================================
    //Login
    //======================================================

    @Test
    void login_shouldReturnAdmin_whenAdminCredentialsAreCorrect() {
        Admin fakeAdmin = new Admin(1, "ali", "pass123", "ali@example.com");
        when(adminFileManager.findAdmin("ali")).thenReturn(fakeAdmin);

        Role result = authService.login("ali", "pass123");

        assertNotNull(result, "Should return admin object");
        assertInstanceOf(Admin.class, result, "Result should be an Admin");
    }

    @Test
    void login_shouldReturnUser_whenUserCredentialsAreCorrect() throws IOException {
        when(adminFileManager.findAdmin("sara")).thenReturn(null);
        writeTempUsersFile("1,sara,pass99,sara@test.com,USER");

        Role result = authService.login("sara", "pass99");

        assertNotNull(result, "Should return user object");
        assertInstanceOf(User.class, result, "Result should be a User");
    }

    @Test
    void login_shouldReturnNull_whenPasswordIsWrong() throws IOException {
        when(adminFileManager.findAdmin("sara")).thenReturn(null);
        writeTempUsersFile("1,sara,pass99,sara@test.com,USER");

        Role result = authService.login("sara", "wrongPassword");

        assertNull(result, "Should return null when password does not match");
    }

    @Test
    void login_shouldReturnNull_whenUserDoesNotExist() throws IOException {
        when(adminFileManager.findAdmin("ghost")).thenReturn(null);
        writeTempUsersFile("1,sara,pass99,sara@test.com,USER");

        Role result = authService.login("ghost", "anyPassword");

        assertNull(result, "Should return null when username not found");
    }

    @Test
    void login_shouldSkipEmptyLines_andStillFindUser() throws IOException {
        when(adminFileManager.findAdmin("sara")).thenReturn(null);
        writeTempUsersFile("", "   ", "1,sara,pass99,sara@test.com,USER");

        Role result = authService.login("sara", "pass99");

        assertNotNull(result, "Should find user even when file has empty lines");
    }

    @Test
    void login_shouldSkipMalformedLines_andStillFindUser() throws IOException {
        when(adminFileManager.findAdmin("sara")).thenReturn(null);
        writeTempUsersFile("CORRUPTED_LINE", "1,sara,pass99,sara@test.com,USER");

        Role result = authService.login("sara", "pass99");

        assertNotNull(result, "Should find user even when file has malformed lines");
    }

    //======================================================
    //requireLoggedIn()
    //======================================================

    @Test
    void requireLoggedIn_shouldPass_whenUserIsLoggedIn() {
        when(session.isLoggedIn()).thenReturn(true);
        assertDoesNotThrow(() -> authService.requireLoggedIn());
    }
    @Test
    void requireLoggedIn_shouldFail_whenUserIsNotLoggedIn() {
        when(session.isLoggedIn()).thenReturn(false);
        assertThrows(AuthorizationException.class, () -> authService.requireLoggedIn());
    }

    //======================================================
    //requireAdmin()
    //======================================================
    @Test
    void requireAdmin_ShouldPass_whenLoggedInAsAdmin() {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> authService.requireAdmin());
    }
    @Test
    void requireAdmin_ShouldFail_whenNotLoggedIn() {
        when(session.isLoggedIn()).thenReturn(false);

        assertThrows(AuthorizationException.class, () -> authService.requireAdmin());
    }
    //======================================================
    //requireUser()
    //======================================================
    @Test
    void requireUser_ShouldPass_whenLoggedInAsUser() {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.isUser()).thenReturn(true);

        assertDoesNotThrow(() -> authService.requireUser());
    }
    @Test
    void requireUser_ShouldFail_whenNotLoggedIn() {
        when(session.isLoggedIn()).thenReturn(false);

        assertThrows(AuthorizationException.class, () -> authService.requireUser());
    }
    @Test
    void requireUser_shouldThrow_whenNotAUser() {
        when(session.isLoggedIn()).thenReturn(true);
        when(session.isUser()).thenReturn(false);

        assertThrows(AuthorizationException.class,
                () -> authService.requireUser());
    }


    //======================================================
    // loginUser() / registerUser() / getUserByUsername()
    //======================================================


    @Test
    void loginUser_shouldReturnTrue_whenCredentialsMatch() throws IOException {
        writeTempUsersFile("1,ahmed,secret,ahmed@test.com,USER");

        boolean result = authService.loginUser("ahmed", "secret");

        assertTrue(result);
    }

    @Test
    void loginUser_shouldReturnFalse_whenPasswordIsWrong() throws IOException {
        writeTempUsersFile("1,ahmed,secret,ahmed@test.com,USER");

        boolean result = authService.loginUser("ahmed", "wrongPass");

        assertFalse(result);
    }

    @Test
    void loginUser_shouldReturnFalse_whenUserDoesNotExist() throws IOException {
        writeTempUsersFile("1,ahmed,secret,ahmed@test.com,USER");

        boolean result = authService.loginUser("nobody", "secret");

        assertFalse(result);
    }

    @Test
    void registerUser_shouldWriteNewUserToFile() throws IOException {
        writeTempUsersFile(); // empty file

        authService.registerUser("sara", "pass99", "sara@test.com");

        // Read the file back and check content was written
        String content = Files.readString(tempDir.resolve("users.txt"));
        assertTrue(content.contains("sara"));
        assertTrue(content.contains("sara@test.com"));
    }

    @Test
    void registerUser_shouldThrowValidationException_whenUsernameIsEmpty() {
        assertThrows(ValidationException.class,
                () -> authService.registerUser("", "pass", "email@test.com"));
    }

    @Test
    void registerUser_shouldThrowValidationException_whenDuplicateUsername() throws IOException {
        writeTempUsersFile("1,sara,pass99,sara@test.com,USER");

        assertThrows(ValidationException.class,
                () -> authService.registerUser("sara", "newPass", "other@test.com"));
    }

}