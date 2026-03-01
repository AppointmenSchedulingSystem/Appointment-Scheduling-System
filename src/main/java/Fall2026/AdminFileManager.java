package Fall2026;

import java.util.ArrayList;
import java.util.List;

public class AdminFileManager {
    private static  final String Admin_File = "Admins.txt";
    private List<Admin> admins = new ArrayList<Admin>();
    private CredentialStorage storage;

    public AdminFileManager() {
        this.storage = new CredentialStorage();
        LoadAdminsfromFile();
        if (admins.isEmpty()){ // will ask in the main
            createDefaultAdmin();
           // System.out.println("No Admins found in file. Please run the first time setup to create an admin account.");
        }
    }


    private void LoadAdminsfromFile() {
        List<String> lines = storage.ReadFromFile(Admin_File);
        if (lines.isEmpty()){
            System.out.println("No admin credentials found in file."+Admin_File);
            return;
        }
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                String adminName = parts[0].trim();
                String adminPassword = parts[1].trim();
                Admin admin = new Admin(adminName, adminPassword);
                admins.add(admin);
            }
            System.out.println("Loaded " + admins.size() + " admins from file.");
        }
    }
    private void createDefaultAdmin() {
        Admin defaultAdmin = new Admin("admin", "admin123");
        admins.add(defaultAdmin);
        saveAdminsToFile();
        System.out.println("Created default admin credentials and saved to file.");
    }

    private void saveAdminsToFile() {
        List<String> lines = new ArrayList<>();
        for (Admin admin : admins) {
            String line = admin.getAdminName() + "," + admin.getAdminPassword();
            lines.add(line);
        }
        if (storage.WriteToFile(Admin_File, lines)) { // print just for testing purposes, it will be removed later
            System.out.println("Admin credentials saved to file successfully.");
        } else {
            System.out.println("Failed to save admin credentials to file.");
        }

    }

    public void addNewAdmin(String username, String password) {
        Admin newAdmin = new Admin(username, password);
        admins.add(newAdmin);
        saveAdminsToFile();
        System.out.println("New admin '" + username + "' added successfully!");
    }

    // Add this method to your AdminFileManager class
    public Admin findAdmin(String username) {
        for (Admin admin : admins) {
            if (admin.getAdminName().equals(username)) {
                return admin;
            }
        }
        return null;

    }


}

