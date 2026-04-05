package Fall2026.shell;

import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.account.User;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class UserShellTest {
    private UserShell userShell;
    private Session session;
    private Scanner scanner;
    private AppointmentService appointmentService;
    private AuthService authService;
    private MockAdminFileManager mockAdminFileManager;
    private MockUserFileManager mockUserFileManager;
    private User testUser;
    private Schedule schedule;
    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        // Capture console output
        output = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(output));

        // Initialize session with user
        session = new Session();
        testUser = new User(1, "testuser", "password123", "user@example.com");
        session.setCurrentAccount(testUser);

        // Initialize schedule and services
        schedule = new Schedule();
        appointmentService = new AppointmentService(schedule);

        // Create mock file managers
        mockAdminFileManager = new MockAdminFileManager();
        mockUserFileManager = new MockUserFileManager();

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
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Available commands"));
        assertTrue(output.contains("help"));
        assertTrue(output.contains("book"));
    }

    @Test
    void runWithEmptyCommandIsIgnored() {
        // Arrange
        String input = "\n\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        assertFalse(session.isLoggedIn());
    }

    @Test
    void runWithUnknownCommandShowsError() {
        // Arrange
        String input = "invalid_command\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Unknown command"));
    }

    @Test
    void runWithSignoutCommand() {
        // Arrange
        String input = "signout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);
        assertTrue(session.isLoggedIn());

        // Act
        userShell.run();

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
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        assertFalse(session.isLoggedIn());
    }

    // ========== Test View Slots Command ==========

    @Test
    void handleViewSlotsWithValidDate() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);

        String input = "slots\n2026-04-10\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Available slots") || output.contains("slots"));
    }

    @Test
    void handleViewSlotsWithInvalidDate() {
        // Arrange
        String input = "slots\ninvalid-date\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("format"));
    }

    @Test
    void handleViewSlotsWithEmptyDate() {
        // Arrange - Add slot for today
        LocalDate today = LocalDate.now();
        TimeSlot todaySlot = new TimeSlot(today, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(todaySlot);

        String input = "slots\n\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Available slots") || output.contains("slots"));
    }

    // ========== Test View All Slots Command ==========

    @Test
    void handleViewAllSlotsWithSlots() {
        // Arrange
        TimeSlot slot1 = new TimeSlot(LocalDate.of(2026, 4, 10), LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimeSlot slot2 = new TimeSlot(LocalDate.of(2026, 4, 11), LocalTime.of(14, 0), LocalTime.of(15, 0));
        schedule.addSlot(slot1);
        schedule.addSlot(slot2);

        String input = "slots all\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Available slots") || output.contains("slots all"));
    }

    @Test
    void handleViewAllSlotsWithNoSlots() {
        // Arrange
        String input = "slots all\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        // Should show available slots (empty list is fine)
        assertTrue(output.contains("Available slots") || output.contains("slots all"));
    }

    // ========== Test Book Command ==========

    @Test
    void handleBookWithValidInput() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);

        String input = "book\n2026-04-10\n1\nCheckup\n5\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("booked") || output.contains("book"));
    }

    @Test
    void handleBookWithInvalidDate() {
        // Arrange
        String input = "book\ninvalid-date\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("format"));
    }

    @Test
    void handleBookWithNoSlotsAvailable() {
        // Arrange
        String input = "book\n2026-04-10\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No available slots") || output.contains("book"));
    }

    @Test
    void handleBookWithInvalidSlotIndex() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);

        String input = "book\n2026-04-10\n999\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("slot number"));
    }

    @Test
    void handleBookWithNonNumericSlotChoice() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);

        String input = "book\n2026-04-10\nabc\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid input") || output.contains("number"));
    }

    @Test
    void handleBookWithInvalidCapacity() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);

        String input = "book\n2026-04-10\n1\nCheckup\nabc\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("positive integer"));
    }

    // ========== Test Cancel Command ==========

    @Test
    void handleCancelWithNoAppointments() {
        // Arrange
        String input = "cancel\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No appointments") || output.contains("cancel"));
    }

    @Test
    void handleCancelWithInvalidIndex() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Checkup", 5);

        String input = "cancel\n999\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("cancel"));
    }

    @Test
    void handleCancelWithNonNumericInput() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Checkup", 5);

        String input = "cancel\nabc\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid input") || output.contains("number"));
    }

    // ========== Test Modify Command ==========

    @Test
    void handleModifyWithNoAppointments() {
        // Arrange
        String input = "modify\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("No appointments") || output.contains("modify"));
    }

    @Test
    void handleModifyWithInvalidIndex() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Checkup", 5);

        String input = "modify\n999\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("modify"));
    }

    @Test
    void handleModifyWithInvalidDate() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Checkup", 5);

        String input = "modify\n1\ninvalid-date\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid") || output.contains("format"));
    }

    @Test
    void handleModifyWithNonNumericIndex() {
        // Arrange
        LocalDate testDate = LocalDate.of(2026, 4, 10);
        TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0));
        schedule.addSlot(slot);
        appointmentService.bookAppointment(slot, "Checkup", 5);

        String input = "modify\nabc\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act
        userShell.run();

        // Assert
        String output = this.output.toString();
        assertTrue(output.contains("Invalid input") || output.contains("number"));
    }

    // ========== Integration Tests ==========

    @Test
    void multipleCommandsSequence() {
        // Arrange
        String input = "help\nhelp\nsignout\n";
        scanner = new Scanner(input);
        userShell = new UserShell(scanner, session, authService, appointmentService);

        // Act & Assert
        assertDoesNotThrow(() -> userShell.run());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void userShellInitializesCorrectly() {
        // Arrange & Act
        userShell = new UserShell(new Scanner(""), session, authService, appointmentService);

        // Assert
        assertNotNull(userShell);
        assertTrue(session.isUser());
    }

    // ========== Mock Classes ==========

    /**
     * Mock AdminFileManager that prevents file I/O
     */
    private static class MockAdminFileManager extends AdminFileManager {
        public MockAdminFileManager() {
            // Prevent file I/O
        }

        @Override
        public void addNewAdmin(String username, String password, String email) {
            // Mock implementation
        }

        @Override
        public Fall2026.domain.account.Admin findAdmin(String username) {
            return null;
        }
    }

    /**
     * Mock UserFileManager that prevents file I/O
     */
    private static class MockUserFileManager extends UserFileManager {
        public MockUserFileManager() {
            // Prevent file I/O
        }

        @Override
        public void addNewUser(String username, String password, String email) {
            // Mock implementation
        }

        @Override
        public User findUser(String username) {
            return null;
        }
    }
}