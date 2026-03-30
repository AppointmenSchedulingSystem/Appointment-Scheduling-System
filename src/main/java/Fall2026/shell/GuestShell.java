package Fall2026.shell;



import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
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

                case "book":
                    handleBookAppointment();
                    break;

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

        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        try {
            authService.registerUser(username, password, email);
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

        // Attempt login (returns User or Admin or null)
        Role account = authService.login(username, password);

        if (account != null) {
            System.out.println("  ✓ Successfully signed in!");

            // Set session
            session.setCurrentAccount(account);

            // Create a single AppointmentService instance
            if (session.isUser()) {
                UserShell userShell = new UserShell(scanner, session, authService, appointmentService);
                userShell.run();
            }

            // Open proper shell based on role
            if (session.isUser()) {
                UserShell userShell = new UserShell(scanner, session, authService, appointmentService);
                userShell.run();
            } else if (session.isAdmin()) {
                AdminShell adminShell = new AdminShell(scanner, session, authService, appointmentService);
                adminShell.run();
            }

        } else {
            System.out.println("  ✗ Invalid username or password.");
        }
    }

    private void handleBookAppointment() {

        try {
            System.out.print("  Enter description: ");
            String description = scanner.nextLine();

            System.out.print("  Enter max capacity: ");
            int capacity = Integer.parseInt(scanner.nextLine());

            // ⚠️ Cast Object to TimeSlot
            TimeSlot slot = (TimeSlot) scheduleFileManager.getAvailableSlots().get(0);

            Appointment appointment = new Appointment(slot, description, capacity);

            // 🔥 IMPORTANT: get logged-in user's email
            User user = (User) session.getCurrentAccount();
            appointment.setUserEmail(user.getEmail());

            appointmentService.bookAppointment(appointment);

            System.out.println("  ✓ Appointment booked successfully!");

        } catch (Exception e) {
            System.out.println("  ✗ " + e.getMessage());
        }
    }

}