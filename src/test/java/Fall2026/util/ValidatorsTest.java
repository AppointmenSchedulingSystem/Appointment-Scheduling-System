package Fall2026.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validators")
class ValidatorsTest {

    @Nested
    @DisplayName("isValidEmail")
    class IsValidEmailTests {

        @Test
        @DisplayName("accepts simple valid email")
        void acceptsSimpleValidEmail() {
            assertTrue(Validators.isValidEmail("user@example.com"));
        }

        @Test
        @DisplayName("accepts email with subdomain")
        void acceptsEmailWithSubdomain() {
            assertTrue(Validators.isValidEmail("user@mail.example.com"));
        }

        @Test
        @DisplayName("accepts email with plus sign")
        void acceptsEmailWithPlusSign() {
            assertTrue(Validators.isValidEmail("user+tag@example.com"));
        }

        @Test
        @DisplayName("accepts email with dots in local part")
        void acceptsEmailWithDotsInLocal() {
            assertTrue(Validators.isValidEmail("first.last@example.com"));
        }

        @Test
        @DisplayName("rejects email missing at-sign")
        void rejectsEmailMissingAtSign() {
            assertFalse(Validators.isValidEmail("userexample.com"));
        }

        @Test
        @DisplayName("rejects email missing domain")
        void rejectsEmailMissingDomain() {
            assertFalse(Validators.isValidEmail("user@"));
        }

        @Test
        @DisplayName("rejects email with leading space")
        void rejectsEmailWithLeadingSpace() {
            assertFalse(Validators.isValidEmail(" user@example.com"));
        }

        @Test
        @DisplayName("rejects email with trailing space")
        void rejectsEmailWithTrailingSpace() {
            assertFalse(Validators.isValidEmail("user@example.com "));
        }

        @Test
        @DisplayName("rejects null email")
        void rejectsNullEmail() {
            assertFalse(Validators.isValidEmail(null));
        }

        @Test
        @DisplayName("rejects empty email")
        void rejectsEmptyEmail() {
            assertFalse(Validators.isValidEmail(""));
        }

        @Test
        @DisplayName("accepts boundary minimal valid email")
        void acceptsBoundaryMinimalValidEmail() {
            assertTrue(Validators.isValidEmail("a@b.c"));
        }
    }

    @Nested
    @DisplayName("isValidPassword")
    class IsValidPasswordTests {

        @Test
        @DisplayName("accepts password with exactly 6 characters")
        void acceptsExactlyLengthSix() {
            assertTrue(Validators.isValidPassword("123456"));
        }

        @Test
        @DisplayName("accepts password with more than 6 characters")
        void acceptsMoreThanSixCharacters() {
            assertTrue(Validators.isValidPassword("1234567"));
        }

        @Test
        @DisplayName("accepts long password")
        void acceptsLongPassword() {
            assertTrue(Validators.isValidPassword("this is a very long password with many characters"));
        }

        @Test
        @DisplayName("accepts password with special characters")
        void acceptsPasswordWithSpecialCharacters() {
            assertTrue(Validators.isValidPassword("p@ssw0rd!"));
        }

        @Test
        @DisplayName("rejects password with 5 characters")
        void rejectsPasswordWithFiveCharacters() {
            assertFalse(Validators.isValidPassword("12345"));
        }

        @Test
        @DisplayName("rejects empty password")
        void rejectsEmptyPassword() {
            assertFalse(Validators.isValidPassword(""));
        }

        @Test
        @DisplayName("rejects null password")
        void rejectsNullPassword() {
            assertFalse(Validators.isValidPassword(null));
        }

        @Test
        @DisplayName("rejects password under minimum length")
        void rejectsPasswordUnderMinimum() {
            assertFalse(Validators.isValidPassword("abc"));
        }
    }

    @Nested
    @DisplayName("isValidUsername")
    class IsValidUsernameTests {

        @Test
        @DisplayName("accepts simple username")
        void acceptsSimpleUsername() {
            assertTrue(Validators.isValidUsername("johndoe"));
        }

        @Test
        @DisplayName("accepts username with numbers")
        void acceptsUsernameWithNumbers() {
            assertTrue(Validators.isValidUsername("user123"));
        }

        @Test
        @DisplayName("accepts username with underscores")
        void acceptsUsernameWithUnderscores() {
            assertTrue(Validators.isValidUsername("user_name"));
        }

        @Test
        @DisplayName("accepts single character username")
        void acceptsSingleCharacterUsername() {
            assertTrue(Validators.isValidUsername("a"));
        }

        @Test
        @DisplayName("accepts username with mixed case")
        void acceptsUsernameMixedCase() {
            assertTrue(Validators.isValidUsername("UserName123"));
        }

        @Test
        @DisplayName("rejects null username")
        void rejectsNullUsername() {
            assertFalse(Validators.isValidUsername(null));
        }

        @Test
        @DisplayName("rejects empty string")
        void rejectsEmptyString() {
            assertFalse(Validators.isValidUsername(""));
        }

        @Test
        @DisplayName("rejects whitespace-only username")
        void rejectsWhitespaceOnlyUsername() {
            assertFalse(Validators.isValidUsername("   "));
        }

        @Test
        @DisplayName("rejects username with leading space")
        void rejectsUsernameWithLeadingSpace() {
            assertFalse(Validators.isValidUsername(" username"));
        }

        @Test
        @DisplayName("rejects username with trailing space")
        void rejectsUsernameWithTrailingSpace() {
            assertFalse(Validators.isValidUsername("username "));
        }
    }
}