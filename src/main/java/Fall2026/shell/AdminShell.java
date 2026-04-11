package Fall2026.shell;

import Fall2026.application.services.AdminAppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.notification.NotificationService;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AdminShell {

    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final AdminFileManager adminFileManager;
    private final ScheduleFileManager scheduleFileManager;
    private final AdminAppointmentService adminAppointmentService;

    public AdminShell(Scanner scanner, Session session, AuthService authService,
                      AppointmentService appointmentService, AdminFileManager adminFileManager,
                      ScheduleFileManager scheduleFileManager) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.adminFileManager = adminFileManager;
        this.scheduleFileManager = scheduleFileManager;
        this.adminAppointmentService = new AdminAppointmentService(appointmentService, authService);
    }

    // ─────────────────────────────────────────────
    // MAIN SHELL
    // ─────────────────────────────────────────────

    public void run() {
        printHelp();

        while (session.isLoggedIn() && session.isAdmin()) {
            System.out.print(GREEN + "admin@system" + RESET + ":~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "schedule":
                    runScheduleShell();
                    break;

                case "reserve":
                    runReserveShell();
                    break;

                case "admin add":
                    handleAddAdmin();
                    break;

                case "signout":
                    authService.logout();
                    System.out.println("  Signed out successfully.");
                    System.out.println();
                    return;

                case "exit":
                    authService.logout();
                    System.out.println();
                    System.out.println("  Goodbye!");
                    System.out.println();
                    System.exit(0);
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
        System.out.println("  help        Show this help message");
        System.out.println("  schedule    Manage time slots");
        System.out.println("  reserve     Manage reservations");
        System.out.println("  admin add   Add a new admin account");
        System.out.println("  signout     Sign out of your account");
        System.out.println("  exit        Exit the system");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // SCHEDULE SUBSHELL
    // ─────────────────────────────────────────────

    private void runScheduleShell() {
        printScheduleHelp();

        while (session.isLoggedIn() && session.isAdmin()) {
            System.out.print(CYAN + "schedule@system" + RESET + ":~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printScheduleHelp();
                    break;

                case "list":
                    handleScheduleList();
                    break;

                case "add":
                    handleScheduleAdd();
                    break;

                case "modify":
                    handleScheduleModify();
                    break;

                case "back":
                    System.out.println();
                    return;

                case "exit":
                    authService.logout();
                    System.out.println();
                    System.out.println("  Goodbye!");
                    System.out.println();
                    System.exit(0);
                    break;

                case "":
                    break;

                default:
                    System.out.println("  Unknown command: '" + input + "'. Type 'help' to see available commands.");
            }
        }
    }

    private void printScheduleHelp() {
        System.out.println();
        System.out.println("  Schedule commands:");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println("  list    View all time slots");
        System.out.println("  add     Add a new slot");
        System.out.println("  modify  Modify an existing slot");
        System.out.println("  back    Return to main menu");
        System.out.println("  exit    Exit the system");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // RESERVE SUBSHELL
    // ─────────────────────────────────────────────

    private void runReserveShell() {
        printReserveHelp();

        while (session.isLoggedIn() && session.isAdmin()) {
            System.out.print(YELLOW + "reserve@system" + RESET + ":~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printReserveHelp();
                    break;

                case "list":
                    handleReserveList();
                    break;

                case "pending":
                    handleReservePending();
                    break;

                case "cancel":
                    handleReserveCancel();
                    break;

                case "modify":
                    handleReserveModify();
                    break;

                case "back":
                    System.out.println();
                    return;

                case "exit":
                    authService.logout();
                    System.out.println();
                    System.out.println("  Goodbye!");
                    System.out.println();
                    System.exit(0);
                    break;

                case "":
                    break;

                default:
                    System.out.println("  Unknown command: '" + input + "'. Type 'help' to see available commands.");
            }
        }
    }

    private void printReserveHelp() {
        System.out.println();
        System.out.println("  Reserve commands:");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println("  list     View all reservations");
        System.out.println("  pending  Approve or reject pending appointments");
        System.out.println("  cancel   Cancel a reservation");
        System.out.println("  modify   Modify a reservation");
        System.out.println("  back     Return to main menu");
        System.out.println("  exit     Exit the system");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // SCHEDULE HANDLERS
    // ─────────────────────────────────────────────

    private void handleScheduleList() {
        List<TimeSlot> slots = new ArrayList<>();
        for (LocalDate date : appointmentService.getAvailableDays()) {
            slots.addAll(appointmentService.getSlotsForDay(date));
        }

        System.out.println();
        System.out.println("  All time slots:");
        System.out.println("  ─────────────────────────────────────────────");

        if (slots.isEmpty()) {
            System.out.println("  No time slots found.");
        } else {
            for (int i = 0; i < slots.size(); i++) {
                TimeSlot slot = slots.get(i);
                System.out.println("  [" + (i + 1) + "] "
                        + slot.getDate() + "  "
                        + slot.getStartTime() + " -> " + slot.getEndTime()
                        + "  |  capacity: " + slot.getMaxCapacity()
                        + "  |  " + slot.getDuration().toMinutes() + " min");
            }
        }
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleScheduleAdd() {
        System.out.print("  Date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        System.out.print("  Start time (HH:MM): ");
        String startInput = scanner.nextLine().trim();

        System.out.print("  End time (HH:MM): ");
        String endInput = scanner.nextLine().trim();

        System.out.print("  Max capacity: ");
        String capacityInput = scanner.nextLine().trim();

        try {
            LocalDate date = LocalDate.parse(dateInput);
            LocalTime start = LocalTime.parse(startInput);
            LocalTime end = LocalTime.parse(endInput);

            if (!end.isAfter(start)) {
                System.out.println("  End time must be after start time.");
                return;
            }

            int maxCapacity = Integer.parseInt(capacityInput);
            if (maxCapacity < 1) {
                System.out.println("  Max capacity must be at least 1.");
                return;
            }

            TimeSlot slot = new TimeSlot(date, start, end, maxCapacity);

            if (scheduleFileManager.hasTimeConflict(slot)) {
                System.out.println("  Time slot conflict detected on " + date + ".");
                return;
            }

            appointmentService.addSlot(slot);
            scheduleFileManager.saveSlotsToFile();
            System.out.println("  Slot added: " + date + "  " + start + " -> " + end);

        } catch (DateTimeParseException e) {
            System.out.println("  Invalid format. Use YYYY-MM-DD and HH:MM.");
        } catch (NumberFormatException e) {
            System.out.println("  Invalid capacity. Enter a number.");
        }
        System.out.println();
    }

    private void handleScheduleModify() {
        List<TimeSlot> slots = new ArrayList<>();
        for (LocalDate date : appointmentService.getAvailableDays()) {
            slots.addAll(appointmentService.getSlotsForDay(date));
        }

        if (slots.isEmpty()) {
            System.out.println("  No time slots to modify.");
            return;
        }

        System.out.println();
        System.out.println("  All time slots:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  capacity: " + slot.getMaxCapacity());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select slot number to modify: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= slots.size()) {
            System.out.println("  Invalid slot number.");
            return;
        }

        TimeSlot oldSlot = slots.get(index);

        System.out.print("  New date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        System.out.print("  New start time (HH:MM): ");
        String startInput = scanner.nextLine().trim();

        System.out.print("  New end time (HH:MM): ");
        String endInput = scanner.nextLine().trim();

        System.out.print("  New max capacity: ");
        String capacityInput = scanner.nextLine().trim();

        try {
            LocalDate newDate = LocalDate.parse(dateInput);

            String[] startParts = startInput.split(":");
            LocalTime newStart = startParts.length == 2
                    ? LocalTime.of(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]))
                    : LocalTime.parse(startInput);

            String[] endParts = endInput.split(":");
            LocalTime newEnd = endParts.length == 2
                    ? LocalTime.of(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]))
                    : LocalTime.parse(endInput);

            if (!newEnd.isAfter(newStart)) {
                System.out.println("  End time must be after start time.");
                return;
            }

            int newCapacity = Integer.parseInt(capacityInput);
            if (newCapacity < 1) {
                System.out.println("  Max capacity must be at least 1.");
                return;
            }

            TimeSlot newSlot = new TimeSlot(newDate, newStart, newEnd, newCapacity);

            for (TimeSlot existing : scheduleFileManager.getAllSlots()) {
                if (!existing.equals(oldSlot) && existing.getDate().equals(newDate)) {
                    if ((newStart.isBefore(existing.getEndTime()) && newEnd.isAfter(existing.getStartTime()))
                            || newStart.equals(existing.getStartTime())) {
                        System.out.println("  Conflict with existing slot: "
                                + existing.getStartTime() + " -> " + existing.getEndTime());
                        return;
                    }
                }
            }

            adminAppointmentService.adminRemoveSlot(oldSlot);
            appointmentService.addSlot(newSlot);
            scheduleFileManager.saveSlotsToFile();
            System.out.println("  Slot modified: " + newDate + "  " + newStart + " -> " + newEnd);

        } catch (DateTimeParseException e) {
            System.out.println("  Invalid format. Use YYYY-MM-DD and HH:MM.");
        } catch (NumberFormatException e) {
            System.out.println("  Invalid capacity. Enter a number.");
        } catch (ValidationException e) {
            System.out.println("  " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // RESERVE HANDLERS
    // ─────────────────────────────────────────────

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
                        + slot.getStartTime() + " -> " + slot.getEndTime()
                        + "  |  " + appt.getAppointmentType().name()
                        + "  |  [" + appt.getStatus().name() + "]"
                        + "  |  " + appt.getCurrentBookings() + "/" + appt.getMaxCapacity());
            }
        }
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleReserveCancel() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        if (appointments.isEmpty()) {
            System.out.println("  No reservations to cancel.");
            return;
        }

        System.out.println();
        System.out.println("  All reservations:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i);
            TimeSlot slot = appt.getTimeSlot();
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  " + appt.getAppointmentType().name()
                    + "  |  [" + appt.getStatus().name() + "]");
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select reservation number to cancel: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  Invalid reservation number.");
            return;
        }

        try {
            adminAppointmentService.adminCancel(appointments.get(index));
            System.out.println("  Reservation cancelled successfully.");
        } catch (ValidationException e) {
            System.out.println("  " + e.getMessage());
        }
        System.out.println();
    }

    private void handleReserveModify() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        if (appointments.isEmpty()) {
            System.out.println("  No reservations to modify.");
            return;
        }

        System.out.println();
        System.out.println("  All reservations:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i);
            TimeSlot slot = appt.getTimeSlot();
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  " + appt.getAppointmentType().name()
                    + "  |  " + appt.getDescription());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select reservation number to modify: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  Invalid reservation number.");
            return;
        }

        Appointment toModify = appointments.get(index);

        System.out.print("  New date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        LocalDate newDate;
        try {
            newDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("  Invalid date format.");
            return;
        }

        List<TimeSlot> slots = appointmentService.getSlotsForDay(newDate);

        if (slots.isEmpty()) {
            System.out.println("  No available slots for " + newDate + ".");
            return;
        }

        System.out.println();
        System.out.println("  Available slots for " + newDate + ":");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  " + slot.getDuration().toMinutes() + " min");
        }

        System.out.print("  Select new slot number: ");
        String slotChoice = scanner.nextLine().trim();

        int slotIndex;
        try {
            slotIndex = Integer.parseInt(slotChoice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (slotIndex < 0 || slotIndex >= slots.size()) {
            System.out.println("  Invalid slot number.");
            return;
        }

        TimeSlot newSlot = slots.get(slotIndex);

        System.out.print("  New description (or press Enter to keep current): ");
        String newDescription = scanner.nextLine().trim();
        if (newDescription.isEmpty()) newDescription = toModify.getDescription();

        try {
            adminAppointmentService.adminModifyFull(toModify, newSlot, newDescription);
            System.out.println("  Reservation modified: "
                    + newSlot.getDate() + "  "
                    + newSlot.getStartTime() + " -> " + newSlot.getEndTime());
        } catch (ValidationException e) {
            System.out.println("  " + e.getMessage());
        }
        System.out.println();
    }

    private void handleReservePending() {
        List<Appointment> all = appointmentService.getAllAppointments();
        List<Appointment> pending = new ArrayList<>();

        for (Appointment appt : all) {
            if (appt.getStatus() == Appointment.AppointmentStatus.PENDING) {
                pending.add(appt);
            }
        }

        if (pending.isEmpty()) {
            System.out.println("  No pending appointments.");
            return;
        }

        System.out.println();
        System.out.println("  Pending appointments:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < pending.size(); i++) {
            Appointment appt = pending.get(i);
            TimeSlot slot = appt.getTimeSlot();
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  " + appt.getAppointmentType().name()
                    + "  |  " + appt.getDescription());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select appointment number: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= pending.size()) {
            System.out.println("  Invalid appointment number.");
            return;
        }

        Appointment selected = pending.get(index);

        System.out.print("  Approve or reject? (approve/reject): ");
        String decision = scanner.nextLine().trim().toLowerCase();

        if (decision.equals("approve")) {
            try {
                adminAppointmentService.adminApprove(selected);
                appointmentService.saveAppointments();
                // TODO: notify user by email once userEmail is stored on Appointment
                System.out.println("  Appointment approved and confirmed.");
            } catch (ValidationException e) {
                System.out.println("  " + e.getMessage());
            }
        } else if (decision.equals("reject")) {
            try {
                adminAppointmentService.adminReject(selected);
                appointmentService.saveAppointments();
                System.out.println("  Appointment rejected and removed.");
            } catch (ValidationException e) {
                System.out.println("  " + e.getMessage());
            }
        } else {
            System.out.println("  Invalid input. Type 'approve' or 'reject'.");
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────
    // ADMIN HANDLERS
    // ─────────────────────────────────────────────

    private void handleAddAdmin() {
        System.out.print("  New admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("  New admin password: ");
        String password = scanner.nextLine().trim();

        System.out.print("  New admin email: ");
        String email = scanner.nextLine().trim();

        try {
            adminFileManager.addNewAdmin(username, password, email);
            System.out.println("  Admin '" + username + "' added successfully.");
        } catch (ValidationException e) {
            System.out.println("  " + e.getMessage());
        }
        System.out.println();
    }
}