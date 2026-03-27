package Fall2026.shell;



import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.account.User;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;

import java.util.Scanner;

public class GuestShell {

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AdminFileManager adminFileManager;
    private final AppointmentService appointmentService;

    public GuestShell(Scanner scanner, Session session, AuthService authService,
                      AppointmentService appointmentService, AdminFileManager adminFileManager) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.adminFileManager = adminFileManager; // ← store it
    }

    public void run() {
        while (true) {
            System.out.print("guest@system:~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "signin":
                    handleSignin();
                    break;

                case "exit":
                    System.out.println();
                    System.out.println("  Goodbye!");
                    System.out.println();
                    return;

                case "":
                    break;

                default:
                    System.out.println("  Unknown command: '" + input + "'. Type 'help' to see available commands.");
            }
        }
    }

    private void printHelp() {
        System.out.println();
        System.out.println("  Available commands:");
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  help       Show this help message");
        System.out.println("  signin     Sign in as admin or user");
        System.out.println("  exit       Exit the system");
        System.out.println("  ─────────────────────────────────────");
        System.out.println();
    }

    private void handleSignin() {
        System.out.print("  Role (admin/user): ");
        String role = scanner.nextLine().trim().toLowerCase();

        if (!role.equals("admin") && !role.equals("user")) {
            System.out.println("  Unknown role '" + role + "'. Type 'admin' or 'user'.");
            return;
        }

        System.out.print("  Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  Password: ");
        String password = scanner.nextLine().trim();

        // ← moved here so both admin and user share the same instance
      //  Schedule schedule = new Schedule();
        //AppointmentService appointmentService = new AppointmentService(schedule);

        try {
            if (role.equals("admin")) {
                authService.loginAdmin(username, password);
                Admin admin = (Admin) session.getCurrentAccount();
                System.out.println("  ✓ Welcome back, " + admin.getUsername() + "!");
                System.out.println();
                AdminShell adminShell = new AdminShell(scanner, session, authService, appointmentService, adminFileManager);
                adminShell.run();

            } else {
                authService.loginUser(username, password);
                User user = (User) session.getCurrentAccount();
                System.out.println("  ✓ Welcome back, " + user.getUsername() + "!");
                System.out.println();
                UserShell userShell = new UserShell(scanner, session, authService, appointmentService); // ← no change needed
                userShell.run();
            }

        } catch (AuthorizationException | ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }
}