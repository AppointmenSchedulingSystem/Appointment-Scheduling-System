package Fall2026.domain.account;

import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminTest {

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin(101, "adminUser", "secret1", "admin@example.com");
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("stores all provided fields correctly")
        void storesProvidedFields() {
            assertEquals(101, admin.getID());
            assertEquals("adminUser", admin.getUsername());
            assertEquals("secret1", admin.getPassword());
            assertEquals("admin@example.com", admin.getEmail());
        }

        @Test
        @DisplayName("keeps last login time null before login is marked")
        void lastLoginTimeStartsNull() {
            assertNull(admin.getLastLoginTime());
        }

        @Test
        @DisplayName("accepts boundary id value of zero")
        void acceptsBoundaryIdZero() {
            Admin boundaryAdmin = new Admin(0, "zeroId", "secret1", "zero@example.com");
            assertEquals(0, boundaryAdmin.getID());
        }
    }

    @Nested
    @DisplayName("setEmail")
    class SetEmailTests {

        @Test
        @DisplayName("updates email when format is valid")
        void updatesEmailForValidFormat() {
            admin.setEmail("new.admin@example.com");
            assertEquals("new.admin@example.com", admin.getEmail());
        }

        @Test
        @DisplayName("throws when email format is invalid")
        void throwsForInvalidFormat() {
            assertThrows(ValidationException.class, () -> admin.setEmail("invalid-email"));
        }

        @Test
        @DisplayName("accepts boundary minimal valid email")
        void acceptsBoundaryMinimalValidEmail() {
            admin.setEmail("a@b.c");
            assertEquals("a@b.c", admin.getEmail());
        }
    }

    @Nested
    @DisplayName("setPassword")
    class SetPasswordTests {

        @Test
        @DisplayName("updates password when valid")
        void updatesPasswordWhenValid() {
            admin.setPassword("updated7");
            assertEquals("updated7", admin.getPassword());
        }

        @Test
        @DisplayName("throws when password has fewer than 6 characters")
        void throwsForPasswordShorterThanSix() {
            assertThrows(ValidationException.class, () -> admin.setPassword("12345"));
        }

        @Test
        @DisplayName("accepts boundary password length of 6")
        void acceptsBoundaryLengthSix() {
            admin.setPassword("123456");
            assertEquals("123456", admin.getPassword());
        }
    }

    @Nested
    @DisplayName("setUsername")
    class SetUsernameTests {

        @Test
        @DisplayName("updates username when valid")
        void updatesUsernameWhenValid() {
            admin.setUsername("newAdmin");
            assertEquals("newAdmin", admin.getUsername());
        }

        @Test
        @DisplayName("throws when username is null")
        void throwsForNullUsername() {
            assertThrows(ValidationException.class, () -> admin.setUsername(null));
        }

        @Test
        @DisplayName("throws when username is empty")
        void throwsForEmptyUsername() {
            assertThrows(ValidationException.class, () -> admin.setUsername(""));
        }

        @Test
        @DisplayName("accepts boundary one-character username")
        void acceptsBoundaryOneCharacterUsername() {
            admin.setUsername("a");
            assertEquals("a", admin.getUsername());
        }
    }
}
