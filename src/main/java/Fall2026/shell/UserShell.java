package Fall2026.shell;

import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.User;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class UserShell {

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AppointmentService appointmentService;

    public UserShell(Scanner scanner, Session session, AuthService authService, AppointmentService appointmentService) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
    }

    public void run() {
        printHelp();

        while (session.isLoggedIn() && session.isUser()) {
            System.out.print("user@system:~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "slots":
                    handleViewSlots();
                    break;

                case "book":
                    handleBook();
                    break;

                case "modify":
                    handleModify();
                    break;

                case "cancel":
                    handleCancel();
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
        System.out.println("  help       Show this help message");
        System.out.println("  slots      View available appointment slots");
        System.out.println("  book       Book an appointment slot");
        System.out.println("  modify     Modify an existing appointment");
        System.out.println("  cancel     Cancel an existing appointment");
        System.out.println("  signout    Sign out of your account");
        System.out.println("  exit       Exit the system");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleViewSlots() {
        System.out.print("  Enter date (YYYY-MM-DD), or press Enter for today: ");
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

    private void handleBook() {
        System.out.print("  Enter date to book (YYYY-MM-DD): ");
        String input = scanner.nextLine().trim();

        LocalDate date;
        try {
            date = LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("  ✗ Invalid date format. Use YYYY-MM-DD (e.g. 2026-04-01).");
            return;
        }

        List<TimeSlot> slots = appointmentService.getSlotsForDay(date);

        if (slots.isEmpty()) {
            System.out.println("  No available slots for " + date + ".");
            return;
        }

        System.out.println();
        System.out.println("  Available slots:");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            System.out.println("  [" + (i + 1) + "] " + slot.getStartTime() + " → " + slot.getEndTime());
        }

        System.out.print("  Select slot number: ");
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

        TimeSlot selectedSlot = slots.get(index);

        System.out.print("  Description (e.g. checkup, follow-up): ");
        String description = scanner.nextLine().trim();

        System.out.print("  Max participants (e.g. 1, 5): ");
        int maxCapacity;
        try {
            maxCapacity = Integer.parseInt(scanner.nextLine().trim());
            if (maxCapacity < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid number. Enter a positive integer.");
            return;
        }

        try {
            appointmentService.bookAppointment(selectedSlot, description, maxCapacity);
            System.out.println("  ✓ Appointment booked: "
                    + selectedSlot.getDate() + "  "
                    + selectedSlot.getStartTime() + " → " + selectedSlot.getEndTime());
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

    private void handleCancel() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        if (appointments.isEmpty()) {
            System.out.println("  No appointments to cancel.");
            return;
        }

        System.out.println();
        System.out.println("  Your appointments:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i);
            TimeSlot slot = appt.getTimeSlot();
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " → " + slot.getEndTime());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select appointment number to cancel: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  ✗ Invalid appointment number.");
            return;
        }

        Appointment toCancel = appointments.get(index);

        try {
            appointmentService.cancelAppointment(toCancel);
            System.out.println("  ✓ Appointment cancelled successfully.");
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

    private void handleModify() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        if (appointments.isEmpty()) {
            System.out.println("  No appointments to modify.");
            return;
        }

        System.out.println();
        System.out.println("  Your appointments:");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i);
            TimeSlot slot = appt.getTimeSlot();
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " → " + slot.getEndTime());
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select appointment number to modify: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  ✗ Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= appointments.size()) {
            System.out.println("  ✗ Invalid appointment number.");
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

        try {
            appointmentService.modifyAppointment(toModify, newSlot);
            System.out.println("  ✓ Appointment modified: "
                    + newSlot.getDate() + "  "
                    + newSlot.getStartTime() + " → " + newSlot.getEndTime());
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }
}