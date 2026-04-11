package Fall2026.shell;

import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.User;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserShell {

    private final Scanner scanner;
    private final Session session;
    private final AuthService authService;
    private final AppointmentService appointmentService;

    private static final String RESET  = "\u001B[0m";
    private static final String PURPLE = "\u001B[35m";

    public UserShell(Scanner scanner, Session session, AuthService authService, AppointmentService appointmentService) {
        this.scanner = scanner;
        this.session = session;
        this.authService = authService;
        this.appointmentService = appointmentService;
    }

    public void run() {
        printHelp();

        while (session.isLoggedIn() && session.isUser()) {
            System.out.print(PURPLE + "user@system" + RESET + ":~$ ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help":
                    printHelp();
                    break;

                case "slots":
                    handleViewSlots();
                    break;

                case "slots all":
                    handleViewAllSlots();
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
        System.out.println("  slots      View available appointment slots (by date)");
        System.out.println("  slots all  View ALL available appointment slots");
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
                Appointment appt = findAppointmentBySlot(slot);
                int remaining = (appt == null) ? slot.getMaxCapacity() : slot.getMaxCapacity() - appt.getCurrentBookings();
                System.out.println("  [" + (i + 1) + "] " + slot.getStartTime() + " → " + slot.getEndTime()
                        + "  │  " + remaining + " spot(s) left");
            }
        }
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private void handleBook() {

        // Step 1: Pick type
        System.out.println();
        System.out.println("  Appointment types:");
        AppointmentType[] types = AppointmentType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + types[i].name());
        }
        System.out.print("  Select appointment type: ");
        String typeChoice = scanner.nextLine().trim();

        int typeIndex;
        try {
            typeIndex = Integer.parseInt(typeChoice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (typeIndex < 0 || typeIndex >= types.length) {
            System.out.println("  Invalid type number.");
            return;
        }

        AppointmentType selectedType = types[typeIndex];

        // Step 2: Collect all compatible slots across all dates
        List<TimeSlot> allCompatible = new ArrayList<>();
        for (LocalDate date : appointmentService.getAvailableDays()) {
            allCompatible.addAll(appointmentService.getSlotsForDay(date, selectedType));
        }

        if (allCompatible.isEmpty()) {
            System.out.println("  No available slots for type " + selectedType.name() + ".");
            return;
        }

        // Step 3: Show them all
        System.out.println();
        System.out.println("  Available slots for " + selectedType.name() + ":");
        System.out.println("  ─────────────────────────────────────────────");
        for (int i = 0; i < allCompatible.size(); i++) {
            TimeSlot slot = allCompatible.get(i);
            System.out.println("  [" + (i + 1) + "] "
                    + slot.getDate() + "  "
                    + slot.getStartTime() + " -> " + slot.getEndTime()
                    + "  |  " + slot.getDuration().toMinutes() + " min");
        }
        System.out.println("  ─────────────────────────────────────────────");

        System.out.print("  Select slot number: ");
        String choice = scanner.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("  Invalid input. Enter a number.");
            return;
        }

        if (index < 0 || index >= allCompatible.size()) {
            System.out.println("  Invalid slot number.");
            return;
        }

        TimeSlot selectedSlot = allCompatible.get(index);

        // Step 4: Description
        System.out.print("  Description (e.g. checkup, follow-up): ");
        String description = scanner.nextLine().trim();

        // Step 5: Book
        try {
            Appointment booked = appointmentService.bookAppointment(
                    selectedSlot, description, selectedSlot.getMaxCapacity(), selectedType);

            if (booked.getStatus() == Appointment.AppointmentStatus.PENDING) {
                System.out.println("  Appointment submitted and awaiting admin approval:");
            } else {
                System.out.println("  Appointment confirmed:");
            }
            System.out.println("     " + selectedSlot.getDate() + "  "
                    + selectedSlot.getStartTime() + " -> " + selectedSlot.getEndTime()
                    + "  |  " + selectedType.name());

        } catch (ValidationException e) {
            System.out.println("  " + e.getMessage());
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
                    + slot.getStartTime() + " → " + slot.getEndTime()
                    + "  │  " + appt.getDescription());
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

        System.out.print("  Enter new description (or press Enter to keep current): ");
        String newDescription = scanner.nextLine().trim();
        if (newDescription.isEmpty()) {
            newDescription = toModify.getDescription();
        }

        try {
            appointmentService.modifyAppointmentFull(toModify, newSlot, newDescription);
            System.out.println("  ✓ Appointment modified: "
                    + newSlot.getDate() + "  "
                    + newSlot.getStartTime() + " → " + newSlot.getEndTime()
                    + "  │  " + newDescription);
        } catch (ValidationException e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        System.out.println();
    }

    private void handleViewAllSlots() {
        List<LocalDate> availableDays = appointmentService.getAvailableDays();

        System.out.println();
        System.out.println("  All available slots:");
        System.out.println("  ─────────────────────────────────────────────");

        if (availableDays.isEmpty()) {
            System.out.println("  No available slots in the system.");
            System.out.println("  ─────────────────────────────────────────────");
            System.out.println();
            return;
        }

        for (LocalDate date : availableDays) {
            List<TimeSlot> slots = appointmentService.getSlotsForDay(date);

            System.out.println("  " + date + ":");
            for (TimeSlot slot : slots) {
                Appointment appt = findAppointmentBySlot(slot);
                int remaining = (appt == null) ? slot.getMaxCapacity() : slot.getMaxCapacity() - appt.getCurrentBookings();
                if (remaining > 0) {
                    System.out.println("    " + slot.getStartTime() + " → " + slot.getEndTime()
                            + "  │  " + remaining + " spot(s) left");
                }
            }
        }

        System.out.println("  ─────────────────────────────────────────────");
        System.out.println();
    }

    private Appointment findAppointmentBySlot(TimeSlot slot) {
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (a.getTimeSlot().equals(slot)) {
                return a;
            }
        }
        return null;
    }
}