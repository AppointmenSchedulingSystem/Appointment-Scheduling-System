package Fall2026.shell;


import Fall2026.application.services.AdminAppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;

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

                case "reserve cancel":
                    handleReserveCancel();
                    break;

                case "reserve modify":
                    handleReserveModify();
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

                case "schedule modify":
                    handleScheduleModify();
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
        System.out.println("  schedule add     Add a new time slot");
        System.out.println("  schedule modify  Modify a time slot (date/time)");
        System.out.println("  reserve list     View all reservations");
        System.out.println("  reserve cancel   Cancel a reservation");
        System.out.println("  reserve modify   Modify a reservation (date/time/description)");
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
            
            // Check for conflict (overlap or same start time)
            if (scheduleFileManager.hasTimeConflict(slot)) {
                System.out.println("  ✗ ERROR: Time slot conflict detected!");
                System.out.println("  ✗ This time overlaps with an existing time slot on " + date + ".");
                System.out.println("  Cannot add conflicting time slot.");
                return;
            }

            appointmentService.addSlot(slot);
            scheduleFileManager.saveSlotsToFile();
            System.out.println("  ✓ Slot added: " + date + "  " + start + " → " + end);
            System.out.println("  ✓ Saved to Slots.txt");

        } catch (DateTimeParseException e) {
            System.out.println("  ✗ Invalid format. Use YYYY-MM-DD and HH:MM.");
        }
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
                    + slot.getStartTime() + " → " + slot.getEndTime()
                    + "  │  Bookings: " + appt.getCurrentBookings() + "/" + appt.getMaxCapacity());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select reservation number to cancel: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  ✗ Invalid reservation number.");
            return;
        }

        Appointment toCancel = appointments.get(index);

        try {
            adminAppointmentService.adminCancel(toCancel);
            System.out.println("  ✓ Reservation cancelled successfully.");
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
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
                    + slot.getStartTime() + " → " + slot.getEndTime()
                    + "  │  " + appt.getDescription()
                    + "  │  Bookings: " + appt.getCurrentBookings() + "/" + appt.getMaxCapacity());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select reservation number to modify: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  ✗ Invalid reservation number.");
            return;
        }

        Appointment toModify = appointments.get(index);

        System.out.print("  Enter new date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        LocalDate newDate;
        try {
            newDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("  ✗ Invalid date format. Use YYYY-MM-DD (e.g. 2026-04-01).");
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
            System.out.println("  [" + (i + 1) + "] " + slot.getStartTime() + " → " + slot.getEndTime());
        }

        System.out.print("  Select new slot number: ");
        String slotChoice = scanner.nextLine().trim();

        int slotIndex;
        try {
            slotIndex = Integer.parseInt(slotChoice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (slotIndex < 0 || slotIndex >= slots.size()) {
            System.out.println("  ✗ Invalid slot number.");
            return;
        }

        TimeSlot newSlot = slots.get(slotIndex);

        System.out.print("  Enter new description (or press Enter to keep current): ");
        String newDescription = scanner.nextLine().trim();
        if (newDescription.isEmpty()) {
            newDescription = toModify.getDescription();
        }

        try {
            adminAppointmentService.adminModifyFull(toModify, newSlot, newDescription);
            System.out.println("  ✓ Reservation modified: "
                    + newSlot.getDate() + "  "
                    + newSlot.getStartTime() + " → " + newSlot.getEndTime()
                    + "  │  " + newDescription);
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

    private void handleScheduleModify() {
        List<TimeSlot> allSlots = appointmentService.getAllAppointments().isEmpty() ?
                appointmentService.getSlotsForDay(LocalDate.now()) :
                appointmentService.getSlotsForDay(LocalDate.now());

        // Get all slots from the schedule
        List<TimeSlot> slots = new java.util.ArrayList<>();
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
                    + slot.getStartTime() + " → " + slot.getEndTime());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select time slot number to modify: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= slots.size()) {
            System.out.println("  ✗ Invalid slot number.");
            return;
        }

        TimeSlot oldSlot = slots.get(index);

        System.out.print("  Enter new date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        LocalDate newDate;
        try {
            newDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("  ✗ Invalid date format. Use YYYY-MM-DD (e.g. 2026-04-01).");
            return;
        }

        System.out.print("  Enter new start time (HH:MM:SS): ");
        String startInput = scanner.nextLine().trim();

        LocalTime newStart;
        try {
            if (startInput.contains(":")) {
                String[] parts = startInput.split(":");
                if (parts.length == 2) {
                    newStart = LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else {
                    newStart = LocalTime.parse(startInput);
                }
            } else {
                newStart = LocalTime.parse(startInput);
            }
        } catch (Exception e) {
            System.out.println("  ✗ Invalid time format. Use HH:MM or HH:MM:SS.");
            return;
        }

        System.out.print("  Enter new end time (HH:MM:SS): ");
        String endInput = scanner.nextLine().trim();

        LocalTime newEnd;
        try {
            if (endInput.contains(":")) {
                String[] parts = endInput.split(":");
                if (parts.length == 2) {
                    newEnd = LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else {
                    newEnd = LocalTime.parse(endInput);
                }
            } else {
                newEnd = LocalTime.parse(endInput);
            }
        } catch (Exception e) {
            System.out.println("  ✗ Invalid time format. Use HH:MM or HH:MM:SS.");
            return;
        }

        if (!newEnd.isAfter(newStart)) {
            System.out.println("  ✗ End time must be after start time.");
            return;
        }

        try {
            TimeSlot newSlot = new TimeSlot(newDate, newStart, newEnd);
            
            // Check for conflict with existing slots (excluding the old slot being modified)
            for (TimeSlot existing : scheduleFileManager.getAllSlots()) {
                if (!existing.equals(oldSlot)) {  // Don't compare with the slot being modified
                    // Check if on same date
                    if (existing.getDate().equals(newDate)) {
                        // Check for time range overlap or same start time
                        if ((newStart.isBefore(existing.getEndTime()) && newEnd.isAfter(existing.getStartTime())) ||
                            newStart.equals(existing.getStartTime())) {
                            System.out.println("  ✗ ERROR: Time slot conflict detected!");
                            System.out.println("  ✗ Slot overlaps with existing slot: " + existing.getStartTime() + " → " + existing.getEndTime());
                            System.out.println("  Cannot modify to conflicting time slot.");
                            return;
                        }
                    }
                }
            }
            
            adminAppointmentService.adminRemoveSlot(oldSlot);
            appointmentService.addSlot(newSlot);
            scheduleFileManager.saveSlotsToFile();
            System.out.println("  ✓ Time slot modified: "
                    + newDate + "  "
                    + newStart + " → " + newEnd);
            System.out.println("  ✓ Saved to Slots.txt");
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

}

