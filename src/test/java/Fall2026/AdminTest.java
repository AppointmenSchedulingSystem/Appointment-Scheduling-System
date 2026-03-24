package Fall2026;

import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AdminTest  {

    Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin("mohanadisgoodmhmh", "admin1234");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void login() {
        // Test valid credentials
        assertTrue(admin.Login("admin", "admin123"));
        assertTrue(admin.IsLoggedIn());

        // Test invalid credentials
        assertFalse(admin.Login("admin", "wrongpassword"));
        assertFalse(admin.Login("wronguser", "admin123"));
    }

    @Test
    void logout() {
        admin.Login("admin", "admin123");
        assertTrue(admin.IsLoggedIn());

        admin.Logout();
        assertFalse(admin.IsLoggedIn());

    }

    @Test
    void isLoggedIn() {
            assertFalse(admin.IsLoggedIn());
            admin.Login("admin", "admin123");
            assertTrue(admin.IsLoggedIn());
    }

    @Test
    void getAdminName() {
        assertEquals("mohanadisgoodmhmh", admin.getAdminName());
    }

    @Test
    void getAdminPassword() {
        assertEquals("admin1234", admin.getAdminPassword());
    }

    @Test
    void getLastLoginTime() {
        assertNull(admin.getLastLoginTime());
        admin.Login("admin", "admin123");
        assertNotNull(admin.getLastLoginTime());
    }
}