package Fall2026.application.services;


import Fall2026.domain.account.Admin;
import Fall2026.domain.account.User;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;

import java.io.*;

//user authentication and authorization logic here, such as login, logout, and permission checks.
public class AuthService {
    private final Session session;
    private final AdminFileManager adminFileManager;
     private final UserFileManager userFileManager ;


    public AuthService(Session session, AdminFileManager adminFileManager , UserFileManager userFileManager ) {
        this.session = session;
        this.adminFileManager = adminFileManager;
        this.userFileManager = userFileManager;
    }

    public Admin loginAdmin(String username, String password) {
        validateLoginInput(username, password);

        Admin admin = adminFileManager.findAdmin(username);
        if (admin == null || !admin.getPassword().equals(password)) {
            throw new AuthorizationException("Invalid admin credentials.");
        }

        // optional: admin.markLoginNow();
        session.setCurrentAccount(admin);
        return admin;
    }
    // Add this when you have user persistence
    public User loginUser(String username, String password) {
        validateLoginInput(username, password);

         User user = userFileManager.findUser(username);
         if (user == null || !user.getPassword().equals(password)) {
             throw new AuthorizationException("Invalid user credentials.");
         }
         session.setCurrentAccount(user);
         return user;
    }
    public void logout() {
        session.clear();
    }

    public void requireLoggedIn() {
        if (!session.isLoggedIn()) {
            throw new AuthorizationException("You must login first.");
        }
    }

    public void requireAdmin() {
        requireLoggedIn();
        if (!session.isAdmin()) {
            throw new AuthorizationException("Admin privileges required.");
        }
    }

    public void requireUser() {
        requireLoggedIn();
        if (!session.isUser()) {
            throw new AuthorizationException("User privileges required.");
        }
    }

    private void validateLoginInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
    }
    public void login(String username, String password) {
        try {
            loginAdmin(username, password);
            return;
        } catch (Exception ignored) {}

        try {
            loginUser(username, password);
            return;
        } catch (Exception ignored) {}

        throw new AuthorizationException("Invalid username or password.");
    }


    public void registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            throw new ValidationException("Username and password cannot be empty.");
        }

        // check if user already exists
        if (userExists(username)) {
            throw new ValidationException("Username already exists.");
        }

        saveUserToFile(username, password);
    }

    private boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    private void saveUserToFile(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error saving user.");
        }
    }
}
