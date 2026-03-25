package Fall2026.util;

public final class Validators {

    private Validators() {}

        public static boolean isValidEmail(String email) {
            return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
        }

        public static boolean isValidPassword(String password) {
            return password != null && password.length() >= 6;
        }

        public static boolean isValidUsername(String username) {
            return username != null && !username.trim().isEmpty();
        }
}
