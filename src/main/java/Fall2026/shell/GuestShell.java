package Fall2026.shell;



import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.account.User;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;

import java.util.Scanner;

public class GuestShell {

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AdminFileManager adminFileManager;
    private final AppointmentService appointmentService;
    private final ScheduleFileManager scheduleFileManager;

    public GuestShell(Scanner scanner, Session session, AuthService authService,
                      AppointmentService appointmentService, AdminFileManager adminFileManager, ScheduleFileManager scheduleFileManager) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.adminFileManager = adminFileManager; // ← store it
        this.scheduleFileManager = scheduleFileManager;
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

                case "signup":
                    handleSignup();
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
        System.out.println("  signup     Create a new user account");
        System.out.println("  exit       Exit the system");
        System.out.println("  ─────────────────────────────────────");
        System.out.println();
    }


    private void handleSignup() {
        System.out.print("  New Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  New Password: ");
        String password = scanner.nextLine().trim();

        try {
            authService.registerUser(username, password);
            System.out.println("  ✓ User created successfully!");
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }
    private void handleSignin() {
        System.out.print("  Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  Password: ");
        String password = scanner.nextLine().trim();

        try {
            // Try login (system decides role internally)
            authService.login(username, password);

            Object account = session.getCurrentAccount();

            if (account instanceof Admin) {
                Admin admin = (Admin) account;
                System.out.println("  ✓ Welcome back, " + admin.getUsername() + "!");
                System.out.println();

                AdminShell adminShell = new AdminShell(scanner, session, authService, appointmentService, adminFileManager,scheduleFileManager);
                adminShell.run();

            } else if (account instanceof User) {
                User user = (User) account;
                System.out.println("  ✓ Welcome back, " + user.getUsername() + "!");
                System.out.println();

                UserShell userShell = new UserShell(scanner, session, authService, appointmentService);
                userShell.run();
            }

        } catch (AuthorizationException | ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }
}