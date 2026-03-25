package Fall2026.infrastructure.persistence;

import Fall2026.domain.account.Admin;

import java.util.ArrayList;
import java.util.List;

public class AdminFileManager {
    private static final String ADMIN_FILE = "Admins.txt";

    private final List<Admin> admins = new ArrayList<>();
    private final CredentialStorage storage;

    public AdminFileManager() {
        this.storage = new CredentialStorage();
        loadAdminsFromFile();
        if (admins.isEmpty()) {
            createDefaultAdmin();
        }
    }

    private void loadAdminsFromFile() {
        List<String> lines = storage.ReadFromFile(ADMIN_FILE);
        if (lines.isEmpty()) {
            System.out.println("No admin credentials found in file: " + ADMIN_FILE);
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

                Admin admin = new Admin(id, username, password, email);
                admins.add(admin);
            } else {
                System.out.println("Skipping invalid admin line (expected 4 parts): " + line);
            }
        }

        System.out.println("Loaded " + admins.size() + " admins from file.");
    }

    private void createDefaultAdmin() {
        // pick id = 1 or compute next id
        Admin defaultAdmin = new Admin(1, "admin", "admin123", "admin@example.com");
        admins.add(defaultAdmin);
        saveAdminsToFile();
        System.out.println("Created default admin credentials and saved to file.");
    }

    private void saveAdminsToFile() {
        List<String> lines = new ArrayList<>();
        for (Admin admin : admins) {
            String line = admin.getID() + "," + admin.getUsername() + "," + admin.getPassword() + "," + admin.getEmail();
            lines.add(line);
        }

        if (storage.WriteToFile(ADMIN_FILE, lines)) {
            System.out.println("Admin credentials saved to file successfully.");
        } else {
            System.out.println("Failed to save admin credentials to file.");
        }
    }

    public void addNewAdmin(String username, String password, String email) {
        int newId = getNextAdminId();
        Admin newAdmin = new Admin(newId, username, password, email);
        admins.add(newAdmin);
        saveAdminsToFile();
        System.out.println("New admin '" + username + "' added successfully!");
    }

    public Admin findAdmin(String username) {
        for (Admin admin : admins) {
            if (admin.getUsername().equals(username)) {
                return admin;
            }
        }
        return null;
    }

    private int getNextAdminId() {
        int max = 0;
        for (Admin admin : admins) {
            if (admin.getID() > max) max = admin.getID();
        }
        return max + 1;
    }
}