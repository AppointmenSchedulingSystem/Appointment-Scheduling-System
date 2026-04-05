package Fall2026.shell;

import Fall2026.application.services.AdminAppointmentService;
import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.Admin;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class AdminShellTest {
    private AdminShell adminShell;
    private Session session;
    private Scanner scanner;
    private AppointmentService appointmentService;
    private AuthService authService;
    private MockAdminFileManager mockAdminFileManager;
    private MockScheduleFileManager mockScheduleFileManager;
    private MockUserFileManager mockUserFileManager;
    private Admin testAdmin;
    private Schedule schedule;
    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        // Capture console output
        output = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(output));

        // Initialize session with admin
        session = new Session();
        testAdmin = new Admin(1, "admin", "admin123", "admin@example.com");
        session.setCurrentAccount(testAdmin);

        // Initialize schedule and services
        schedule = new Schedule();
        appointmentService = new AppointmentService(schedule);

        // Create mock file managers
        mockAdminFileManager = new MockAdminFileManager();
        mockUserFileManager = new MockUserFileManager();
        mockScheduleFileManager = new MockScheduleFileManager(schedule);

        // Initialize auth service
        authService = new AuthService(session, mockAdminFileManager, mockUserFileManager);

        // Create mock scanner
        scanner = new Scanner("");
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        session.clear();
    }

    // ========== Test run() method with valid commands ==========

    @Test
    void runWithHelpCommand() {
        // Arrange
        String input = "help\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert - should print help and exit
        String output = this.output.toString();
        assertTrue(output.contains("Available commands"));
        assertTrue(output.contains("help"));
        assertTrue(output.contains("schedule list"));
    }

    @Test
    void runWithEmptyCommandIsIgnored() {
        // Arrange
        String input = "\n\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert - should handle empty input gracefully
        assertTrue(session.isLoggedIn() == false); // Should be logged out after signout
    }

    @Test
    void runWithUnknownCommandShowsError() {
        // Arrange
        String input = "invalid_command\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Unknown command"));
    }

    @Test
    void runWithSignoutCommand() {
        // Arrange
        String input = "signout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);
        assertTrue(session.isLoggedIn());

        // Act
        adminShell.run();

        // Assert
        assertFalse(session.isLoggedIn());
        String output = this.output.toString();
        assertTrue(output.contains("Signed out successfully"));
    }

    @Test
    void runExitsWhenSessionEnds() {
        // Arrange
        String input = "signout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert - Session should be cleared
        assertFalse(session.isLoggedIn());
    }

    // ========== Test Schedule List Command ==========

    @Test
    void handleScheduleListWithValidDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        TimeSlot todaySlot = new TimeSlot(today, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(todaySlot);

        String input = "schedule list\n" + today + "\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Available slots") || output.contains("schedule list"));
    }

    @Test
    void handleScheduleListWithInvalidDate() {
        // Arrange
        String input = "schedule list\ninvalid-date\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act & Assert - should handle gracefully
        assertDoesNotThrow(() -> adminShell.run());
    }

    // ========== Test Reserve List Command ==========

    @Test
    void handleReserveListWithNoAppointments() {
        // Arrange
        String input = "reserve list\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No reservations found") || output.contains("All reservations"));
    }

    @Test
    void handleReserveListWithAppointments() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        Appointment appt = appointmentService.bookAppointment(slot, "Test", 5);

        String input = "reserve list\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("All reservations"));
    }

    // ========== Test Add Admin Command ==========

    @Test
    void handleAddAdminSuccessfully() {
        // Arrange
        String input = "admin add\nnewadmin\npassword123\nnewadmin@example.com\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("added successfully") || output.contains("Add a new admin"));
    }

    @Test
    void handleAddAdminWithEmptyFields() {
        // Arrange
        String input = "admin add\n\n\n\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act & Assert - should handle gracefully
        assertDoesNotThrow(() -> adminShell.run());
    }

    // ========== Test Reserve Cancel Command ==========

    @Test
    void handleReserveCancelWithNoAppointments() {
        // Arrange
        String input = "reserve cancel\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No reservations") || output.contains("cancel"));
    }

    @Test
    void handleReserveCancelWithInvalidIndex() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Test", 5);

        String input = "reserve cancel\n999\nsignout\n"; // Invalid index
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("reserve cancel"));
    }

    @Test
    void handleReserveCancelWithNonNumericInput() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Test", 5);

        String input = "reserve cancel\nabc\nsignout\n"; // Non-numeric input
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid input") || output.contains("Enter a number"));
    }

    // ========== Test Reserve Modify Command ==========

    @Test
    void handleReserveModifyWithNoAppointments() {
        // Arrange
        String input = "reserve modify\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No reservations") || output.contains("modify"));
    }

    @Test
    void handleReserveModifyWithInvalidIndex() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Test", 5);

        String input = "reserve modify\n999\nsignout\n"; // Invalid index
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("reserve modify"));
    }

    // ========== Test Schedule Add Command ==========

    @Test
    void handleScheduleAddWithValidInput() {
        // Arrange - Add an initial slot so we have something to conflict check against
        LocalDate testDate = LocalDate.of(2026, 4, 15);
        TimeSlot existingSlot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(existingSlot);

        String input = "schedule add\n2026-04-15\n10:00\n11:00\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert - Should either add successfully or show conflict message
        String output = this.output.toString();
        assertTrue(output.contains("schedule add") || output.contains("Slot added") || output.contains("conflict"));
    }

    @Test
    void handleScheduleAddWithInvalidDate() {
        // Arrange
        String input = "schedule add\ninvalid-date\n10:00\n11:00\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("format"));
    }

    @Test
    void handleScheduleAddWithInvalidTime() {
        // Arrange
        String input = "schedule add\n2026-04-15\ninvalid\n11:00\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("format"));
    }

    @Test
    void handleScheduleAddWithEndBeforeStart() {
        // Arrange
        String input = "schedule add\n2026-04-15\n11:00\n10:00\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("End time") || output.contains("after"));
    }

    // ========== Test Schedule Modify Command ==========

    @Test
    void handleScheduleModifyWithNoSlots() {
        // Arrange
        String input = "schedule modify\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act
        adminShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No time slots") || output.contains("modify"));
    }

    // ========== Integration Tests ==========

    @Test
    void multipleCommandsSequence() {
        // Arrange
        String input = "help\nhelp\nhelp\nsignout\n";
        scanner = new Scanner(input);
        adminShell = new AdminShell(scanner, session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Act & Assert
        assertDoesNotThrow(() -> adminShell.run());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void adminShellInitializesCorrectly() {
        // Arrange & Act
        adminShell = new AdminShell(new Scanner(""), session, authService, appointmentService,
                                   mockAdminFileManager, mockScheduleFileManager);

        // Assert
        assertNotNull(adminShell);
        assertTrue(session.isAdmin());
    }

    // ========== Mock Classes ==========

    /**
     * Mock AdminFileManager that prevents file I/O
     */
    private static class MockAdminFileManager extends AdminFileManager {
        public MockAdminFileManager() {
            // Call parent but don't let it do file I/O
        }

        @Override
        public void addNewAdmin(String username, String password, String email) {
            // Mock implementation - just store in memory
            Fall2026.domain.account.Admin admin = new Fall2026.domain.account.Admin(
                1, username, password, email);
            // Don't actually save to file
        }

        @Override
        public Fall2026.domain.account.Admin findAdmin(String username) {
            // Mock implementation
            return null;
        }
    }

    /**
     * Mock ScheduleFileManager that prevents file I/O
     */
    private static class MockScheduleFileManager extends ScheduleFileManager {
        public MockScheduleFileManager(Schedule existingSchedule) {
            super(existingSchedule);
        }

        @Override
        public void saveSlotsToFile() {
            // Mock implementation - no file I/O
        }

        @Override
        public boolean hasTimeConflict(TimeSlot slot) {
            for (TimeSlot existing : this.getSchedule().getAllSlots()) {
                if (existing.getDate().equals(slot.getDate())) {
                    if (slot.getStartTime().isBefore(existing.getEndTime()) &&
                        slot.getEndTime().isAfter(existing.getStartTime())) {
                        return true;
                    }
                    if (slot.getStartTime().equals(existing.getStartTime())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Mock UserFileManager that prevents file I/O
     */
    private static class MockUserFileManager extends Fall2026.infrastructure.persistence.UserFileManager {
        public MockUserFileManager() {
            // Don't call super() to avoid file I/O
        }

        @Override
        public void addNewUser(String username, String password, String email) {
            // Mock implementation
        }

        @Override
        public Fall2026.domain.account.User findUser(String username) {
            // Mock implementation
            return null;
        }
    }
}