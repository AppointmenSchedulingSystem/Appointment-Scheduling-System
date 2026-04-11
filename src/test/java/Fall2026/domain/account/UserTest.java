package Fall2026.domain.account;

import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(202, "regularUser", "secure1", "user@example.com");
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("stores all provided fields correctly")
        void storesProvidedFields() {
            assertEquals(202, user.getID());
            assertEquals("regularUser", user.getUsername());
            assertEquals("secure1", user.getPassword());
            assertEquals("user@example.com", user.getEmail());
        }

        @Test
        @DisplayName("accepts boundary id value of zero")
        void acceptsBoundaryIdZero() {
            User boundaryUser = new User(0, "zeroUser", "secure1", "zero@example.com");
            assertEquals(0, boundaryUser.getID());
        }

        @Test
        @DisplayName("accepts valid mixed-case username and email values")
        void acceptsMixedCaseValues() {
            User mixed = new User(1, "UserName123", "secure1", "User.Name@example.com");
            assertEquals("UserName123", mixed.getUsername());
            assertEquals("User.Name@example.com", mixed.getEmail());
        }
    }

    @Nested
    @DisplayName("setEmail")
    class SetEmailTests {

        @Test
        @DisplayName("updates email when format is valid")
        void updatesEmailForValidFormat() {
            user.setEmail("updated.user@example.com");
            assertEquals("updated.user@example.com", user.getEmail());
        }

        @Test
        @DisplayName("throws when email format is invalid")
        void throwsForInvalidFormat() {
            assertThrows(ValidationException.class, () -> user.setEmail("invalid-email"));
        }

        @Test
        @DisplayName("accepts boundary minimal valid email")
        void acceptsBoundaryMinimalValidEmail() {
            user.setEmail("a@b.c");
            assertEquals("a@b.c", user.getEmail());
        }
    }

    @Nested
    @DisplayName("setPassword")
    class SetPasswordTests {

        @Test
        @DisplayName("updates password when valid")
        void updatesPasswordWhenValid() {
            user.setPassword("updated7");
            assertEquals("updated7", user.getPassword());
        }

        @Test
        @DisplayName("throws when password has fewer than 6 characters")
        void throwsForPasswordShorterThanSix() {
            assertThrows(ValidationException.class, () -> user.setPassword("12345"));
        }

        @Test
        @DisplayName("accepts boundary password length of 6")
        void acceptsBoundaryLengthSix() {
            user.setPassword("123456");
            assertEquals("123456", user.getPassword());
        }
    }

    @Nested
    @DisplayName("setUsername")
    class SetUsernameTests {

        @Test
        @DisplayName("updates username when valid")
        void updatesUsernameWhenValid() {
            user.setUsername("newUser");
            assertEquals("newUser", user.getUsername());
        }

        @Test
        @DisplayName("throws when username is null")
        void throwsForNullUsername() {
            assertThrows(ValidationException.class, () -> user.setUsername(null));
        }

        @Test
        @DisplayName("throws when username is empty")
        void throwsForEmptyUsername() {
            assertThrows(ValidationException.class, () -> user.setUsername(""));
        }

        @Test
        @DisplayName("accepts boundary one-character username")
        void acceptsBoundaryOneCharacterUsername() {
            user.setUsername("u");
            assertEquals("u", user.getUsername());
        }
    }
}
