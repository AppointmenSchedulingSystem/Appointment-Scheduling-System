package Fall2026.application.services;


import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
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
    public boolean loginUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] parts = line.split(",");
                if (parts.length < 3) continue; // skip malformed lines
                String fileUsername = parts[1]; // username at index 1
                String filePassword = parts[2]; // password at index 2
                if (fileUsername.equals(username) && filePassword.equals(password)) {
                    return true; // correct login
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // login failed
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


    public void registerUser(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            throw new ValidationException("Username, password, and email cannot be empty.");
        }

        if (userExists(username)) {
            throw new ValidationException("Username already exists.");
        }

        int newId = 1;

        // Get last used ID
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 1) continue;
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id >= newId) newId = id + 1;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}

        // Append new user with role USER by default
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(newId + "," + username + "," + password + "," + email + ",USER");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error saving user.", e);
        }
    }
    private boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] parts = line.split(",");
                if (parts.length < 2) continue; // skip malformed lines
                if (parts[1].equals(username)) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    private void saveUserToFile(String username, String password, String email) {
        int newId = 1; // default first ID

        // 1️⃣ Read the file to find the last used ID
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] parts = line.split(",");
                if (parts.length < 1) continue; // skip malformed lines
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id >= newId) newId = id + 1; // increment to get new ID
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {
            // file might not exist yet, that's okay
        }

        // 2️⃣ Append the new user to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(newId + "," + username + "," + password + "," + email);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error saving user.", e);
        }
    }

    public User getUserByUsername(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 4) continue; // ID, username, password, email
                if (parts[1].equals(username)) {
                    int id = Integer.parseInt(parts[0]);
                    String password = parts[2];
                    String email = parts[3];
                    return new User(id, username, password, email);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Role login(String username, String password) {
        // First check admins
        Admin admin = loginAdminSafe(username, password);
        if (admin != null) return admin;

        // Then check users
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines

                String[] parts = line.split(","); // ID,username,password,email
                if (parts.length < 4) continue; // skip malformed lines

                String fileUsername = parts[1];
                String filePassword = parts[2];
                String email = parts[3];

                if (fileUsername.equals(username) && filePassword.equals(password)) {
                    // Create User object and set session
                    User user = new User(Integer.parseInt(parts[0]), username, password, email);
                    session.setCurrentAccount(user);
                    return user; // login successful
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // login failed
    }

    private Admin loginAdminSafe(String username, String password) {
        try {
            Admin admin = loginAdmin(username, password);
            if (admin != null) {
                session.setCurrentAccount(admin);
            }
            return admin;
        } catch (Exception ignored) {}
        return null;
    }

}
