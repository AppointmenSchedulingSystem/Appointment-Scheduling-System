package Fall2026.shell;


import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AdminShell {

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final AdminFileManager adminFileManager;

    public AdminShell(Scanner scanner, Session session, AuthService authService,
                      AppointmentService appointmentService, AdminFileManager adminFileManager) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.adminFileManager = adminFileManager; // ← same instance as AuthService uses
    }

    public void run() {
        Admin admin = (Admin) session.getCurrentAccount();
        printHelp();

        while (session.isLoggedIn() && session.isAdmin()) {
            System.out.print("admin@system:~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "schedule list":
                    handleScheduleList();
                    break;

                case "reserve list":
                    handleReserveList();
                    break;

                case "admin add":
                    handleAddAdmin();
                    break;

                case "signout":
                    authService.logout();
                    System.out.println("  ✓ Signed out successfully.");
                    System.out.println();
                    return;

                case "exit":
                    authService.logout();
                    System.out.println();
                    System.out.println("  Goodbye!");
                    System.out.println();
                    System.exit(0);
                    break;

                case "schedule add":
                    handleScheduleAdd();
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
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println("  help             Show this help message");
        System.out.println("  schedule list    View all available time slots");
        System.out.println("  reserve list     View all reservations");
        System.out.println("  schedule add     Add a new time slot");
        System.out.println("  admin add        Add a new admin account");
        System.out.println("  signout          Sign out of your account");
        System.out.println("  exit             Exit the system");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleScheduleList() {
        System.out.print("  Enter date to view (YYYY-MM-DD), or press Enter for today: ");
        String input = scanner.nextLine().trim();

        LocalDate date;
        if (input.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("  ✗ Invalid date format. Use YYYY-MM-DD (e.g. 2026-04-01).");
                return;
            }
        }

        List<TimeSlot> slots = appointmentService.getSlotsForDay(date);
        System.out.println();
        System.out.println("  Available slots for " + date + ":");
        System.out.println("  ─────────────────────────────────────────────");

        if (slots.isEmpty()) {
            System.out.println("  No available slots for this date.");
        } else {
            for (int i = 0; i < slots.size(); i++) {
                TimeSlot slot = slots.get(i);
                System.out.println("  [" + (i + 1) + "] " + slot.getStartTime() + " → " + slot.getEndTime());
            }
        }
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleReserveList() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        System.out.println();
        System.out.println("  All reservations:");
        System.out.println("  ─────────────────────────────────────────────");

        if (appointments.isEmpty()) {
            System.out.println("  No reservations found.");
        } else {
            for (int i = 0; i < appointments.size(); i++) {
                Appointment appt = appointments.get(i);
                TimeSlot slot = appt.getTimeSlot();
                System.out.println("  [" + (i + 1) + "] "
                        + slot.getDate() + "  "
                        + slot.getStartTime() + " → " + slot.getEndTime()
                        + "  │  Bookings: " + appt.getCurrentBookings() + "/" + appt.getMaxCapacity());
            }
        }
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleAddAdmin() {
        System.out.print("  New admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  New admin password: ");
        String password = scanner.nextLine().trim();

        System.out.print("  New admin email: ");
        String email = scanner.nextLine().trim();

        try {
            adminFileManager.addNewAdmin(username, password, email); // ← shared instance
            System.out.println("  ✓ Admin '" + username + "' added successfully.");
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

    private void handleScheduleAdd() {
        System.out.print("  Date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        System.out.print("  Start time (HH:MM): ");
        String startInput = scanner.nextLine().trim();

        System.out.print("  End time (HH:MM): ");
        String endInput = scanner.nextLine().trim();

        try {
            LocalDate date = LocalDate.parse(dateInput);
            LocalTime start = LocalTime.parse(startInput);
            LocalTime end = LocalTime.parse(endInput);

            if (!end.isAfter(start)) {
                System.out.println("  ✗ End time must be after start time.");
                return;
            }

            TimeSlot slot = new TimeSlot(date, start, end);
            appointmentService.addSlot(slot);
            System.out.println("  ✓ Slot added: " + date + "  " + start + " → " + end);

        } catch (DateTimeParseException e) {
            System.out.println("  ✗ Invalid format. Use YYYY-MM-DD and HH:MM.");
        }
        System.out.println();
    }


}