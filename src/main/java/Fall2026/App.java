package Fall2026;

import java.util.Scanner;

public class App {

    private static void WelcomeToSystem() {
        System.out.println("════════════════════════════════════════");
        System.out.println("WELCOME TO Appointment Scheduling System");
        System.out.println("════════════════════════════════════════");
    }

    // Function 1: Setup admin (creates default if none exists)
    public static AdminFileManager setupAdmin() {
        AdminFileManager manager = new AdminFileManager();
        // AdminFileManager already creates default admin if none exists
        // Default: username="admin", password="admin123"
        return manager;
    }

    // Function 2: Login function
    public static Admin loginAdmin(AdminFileManager manager) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== ADMIN LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Find the admin
        Admin admin = manager.findAdmin(username);

        // Try to login
        if (admin != null && admin.Login(username, password)) {
            System.out.println("✓ Login successful! Welcome to admin page.");
            return admin;  // Return logged in admin
        } else {
            System.out.println("False info, try again.");
            return null;  // Return null if login failed
        }
    }

    // Function 3: Admin page (after successful login)
    public static void adminPage(Admin admin, AdminFileManager manager) {
        Scanner scanner = new Scanner(System.in);

        while (admin.IsLoggedIn()) {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("           ADMIN PAGE");
            System.out.println("════════════════════════════════════════");
            System.out.println("Welcome, " + admin.getAdminName() + "!");
            System.out.println("1. LATER");
            System.out.println("2. LATER");
            System.out.println("3. Add New Admin");
            System.out.println("4. Logout");
            System.out.println("════════════════════════════════════════");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("→ Managing schedules... (Coming soon)");
                    break;
                case "2":
                    System.out.println("→ Managing reservations... (Coming soon)");
                    break;
                case "3":
                    System.out.print("New admin username: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("New admin password: ");
                    String newPassword = scanner.nextLine();
                    manager.addNewAdmin(newUsername, newPassword);
                    break;
                case "4":
                    admin.Logout();
                    System.out.println("✓ Logged out successfully. Session closed.");
                    break;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }

    public static void main(String[] args) {
        // Welcome message
        WelcomeToSystem();

        // Step 1: Setup admin (creates default if none exists)
        AdminFileManager manager = setupAdmin();

        // Step 2: Login loop (keeps asking until login succeeds)
        Admin loggedInAdmin = null;
        while (loggedInAdmin == null) {
            loggedInAdmin = loginAdmin(manager);
            // If login fails, loggedInAdmin is null, loop continues
        }

        // Step 3: Show admin page
        adminPage(loggedInAdmin, manager);

        // Exit
        System.out.println("\n════════════════════════════════════════");
        System.out.println("Thank you for using the system. Goodbye!");
        System.out.println("════════════════════════════════════════");
    }
}