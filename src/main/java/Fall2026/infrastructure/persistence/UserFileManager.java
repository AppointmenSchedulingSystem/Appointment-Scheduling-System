package Fall2026.infrastructure.persistence;

import Fall2026.domain.account.User;

import java.util.ArrayList;
import java.util.List;

public class UserFileManager {
    private static final String USER_FILE = "Users.txt";
    private final List<User> users = new ArrayList<>();
    private final CredentialStorage storage;

    public UserFileManager() {
        this.storage = new CredentialStorage();
        loadUsersFromFile();
        if (users.isEmpty()) {
            createDefaultUser(); // optional but helpful for testing
        }
    }
    private void loadUsersFromFile() {
        List<String> lines = storage.ReadFromFile(USER_FILE);
        if (lines.isEmpty()) {
            System.out.println("No user credentials found in file: " + USER_FILE);
            return;
        }

        for (String line : lines) {
            String[] parts = line.split(",");
            // expected: id,username,password,email
            if (parts.length == 4) {
                int id = Integer.parseInt(parts[0].trim());
                String username = parts[1].trim();
                String password = parts[2].trim();
                String email = parts[3].trim();

                users.add(new User(id, username, password, email));
            } else {
                System.out.println("Skipping invalid user line (expected 4 parts): " + line);
            }
        }

        System.out.println("Loaded " + users.size() + " users from file.");
    }
    private void saveUsersToFile() {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.getID() + "," + user.getUsername() + "," + user.getPassword() + "," + user.getEmail());
        }
        storage.WriteToFile(USER_FILE, lines);
    }
    private void createDefaultUser() {
        User defaultUser = new User(1, "user", "user123", "user@example.com");
        users.add(defaultUser);
        saveUsersToFile();
        System.out.println("Created default user credentials and saved to file.");
    }
    private int getNextUserId() {
        int max = 0;
        for (User user : users) {
            if (user.getID() > max) max = user.getID();
        }
        return max + 1;
    }

    public void addNewUser(String username, String password, String email) {
        int id = getNextUserId();
        User newUser = new User(id, username, password, email);
        users.add(newUser);
        saveUsersToFile();
        System.out.println("New user '" + username + "' added successfully!");
    }

    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }


}
