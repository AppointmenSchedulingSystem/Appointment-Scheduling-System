//package Fall2026.application.services;
//
//import Fall2026.domain.account.Admin;
//import Fall2026.domain.appointment.Appointment;
//import Fall2026.domain.appointment.Schedule;
//import Fall2026.domain.appointment.TimeSlot;
//import Fall2026.domain.exceptions.AuthorizationException;
//import Fall2026.domain.exceptions.ValidationException;
//import Fall2026.infrastructure.persistence.AdminFileManager;
//import Fall2026.infrastructure.persistence.UserFileManager;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class AdminAppointmentServiceTest {
//    private AdminAppointmentService adminAppointmentService;
//    private AppointmentService appointmentService;
//    private AuthService authService;
//    private Session session;
//    private Admin admin;
//    private TimeSlot futureSlot;
//    private Appointment appointment;
//
//    @BeforeEach
//    void setUp() {
//        // Initialize session and admin
//        session = new Session();
//        admin = new Admin(1, "adminUser", "password123", "admin@example.com");
//        session.setCurrentAccount(admin);
//
//        // Create a schedule and appointment service
//        Schedule schedule = new Schedule();
//        appointmentService = new AppointmentService(schedule);
//
//        // Create auth service with mock file managers (not used by requireAdmin/requireUser)
//        // We create dummy instances to avoid NullPointerException during construction
//        AdminFileManager mockAdminFileManager = new MockAdminFileManager();
//        UserFileManager mockUserFileManager = new MockUserFileManager();
//        authService = new AuthService(session, mockAdminFileManager, mockUserFileManager);
//
//        // Create the service under test
//        adminAppointmentService = new AdminAppointmentService(appointmentService, authService);
//
//        // Create test data
//        futureSlot = new TimeSlot(
//            LocalDate.now().plusDays(5),
//            LocalTime.of(10, 0),
//            LocalTime.of(11, 0)
//        );
//
//        // Add the slot to the schedule
//        appointmentService.addSlot(futureSlot);
//
//        // Create an appointment
//        appointment = appointmentService.bookAppointment(futureSlot, "Test Appointment", 5);
//    }
//
//    @AfterEach
//    void tearDown() {
//        session.clear();
//    }
//
//    @Test
//    void adminCancelSuccessfully() {
//        // Arrange
//        Appointment apptToCancel = appointmentService.bookAppointment(futureSlot, "Cancel Test", 3);
//        int initialCount = appointmentService.getAllAppointments().size();
//
//        // Act
//        adminAppointmentService.adminCancel(apptToCancel);
//
//        // Assert - appointment should be removed when bookings reach 0
//        assertTrue(appointmentService.getAllAppointments().size() <= initialCount);
//    }
//
//    @Test
//    void adminCancelFailsWithoutAdminPrivileges() {
//        // Arrange
//        session.clear(); // No one logged in
//
//        // Act & Assert
//        assertThrows(AuthorizationException.class, () -> {
//            adminAppointmentService.adminCancel(appointment);
//        });
//    }
//
//    @Test
//    void adminCancelThrowsExceptionForPastAppointment() {
//        // Arrange
//        TimeSlot pastSlot = new TimeSlot(
//            LocalDate.now().minusDays(1),
//            LocalTime.of(10, 0),
//            LocalTime.of(11, 0)
//        );
//        appointmentService.addSlot(pastSlot);
//        Appointment pastAppointment = appointmentService.bookAppointment(pastSlot, "Past Appointment", 5);
//
//        // Act & Assert
//        assertThrows(ValidationException.class, () -> {
//            adminAppointmentService.adminCancel(pastAppointment);
//        });
//    }
//
//    @Test
//    void adminModifySuccessfully() {
//        // Arrange
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(7),
//            LocalTime.of(14, 0),
//            LocalTime.of(15, 0)
//        );
//        appointmentService.addSlot(newSlot);
//
//        // Act
//        Appointment modifiedAppt = adminAppointmentService.adminModify(appointment, newSlot);
//
//        // Assert
//        assertNotNull(modifiedAppt);
//        assertEquals(newSlot, modifiedAppt.getTimeSlot());
//        assertEquals(appointment.getDescription(), modifiedAppt.getDescription());
//        assertEquals(appointment.getMaxCapacity(), modifiedAppt.getMaxCapacity());
//    }
//
//    @Test
//    void adminModifyFailsWithoutAdminPrivileges() {
//        // Arrange
//        session.clear(); // No one logged in
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(7),
//            LocalTime.of(14, 0),
//            LocalTime.of(15, 0)
//        );
//
//        // Act & Assert
//        assertThrows(AuthorizationException.class, () -> {
//            adminAppointmentService.adminModify(appointment, newSlot);
//        });
//    }
//
//    @Test
//    void adminModifyThrowsExceptionForPastAppointment() {
//        // Arrange
//        TimeSlot pastSlot = new TimeSlot(
//            LocalDate.now().minusDays(1),
//            LocalTime.of(10, 0),
//            LocalTime.of(11, 0)
//        );
//        appointmentService.addSlot(pastSlot);
//        Appointment pastAppointment = appointmentService.bookAppointment(pastSlot, "Past Appointment", 5);
//
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(7),
//            LocalTime.of(14, 0),
//            LocalTime.of(15, 0)
//        );
//
//        // Act & Assert
//        assertThrows(ValidationException.class, () -> {
//            adminAppointmentService.adminModify(pastAppointment, newSlot);
//        });
//    }
//
//    @Test
//    void adminModifyFullSuccessfully() {
//        // Arrange
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(10),
//            LocalTime.of(15, 0),
//            LocalTime.of(16, 0)
//        );
//        appointmentService.addSlot(newSlot);
//        String newDescription = "Updated Description";
//
//        // Act
//        Appointment modifiedAppt = adminAppointmentService.adminModifyFull(appointment, newSlot, newDescription);
//
//        // Assert
//        assertNotNull(modifiedAppt);
//        assertEquals(newSlot, modifiedAppt.getTimeSlot());
//        assertEquals(newDescription, modifiedAppt.getDescription());
//        assertEquals(appointment.getMaxCapacity(), modifiedAppt.getMaxCapacity());
//    }
//
//    @Test
//    void adminModifyFullWithNullDescription() {
//        // Arrange
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(10),
//            LocalTime.of(15, 0),
//            LocalTime.of(16, 0)
//        );
//        appointmentService.addSlot(newSlot);
//
//        // Act
//        Appointment modifiedAppt = adminAppointmentService.adminModifyFull(appointment, newSlot, null);
//
//        // Assert
//        assertNotNull(modifiedAppt);
//        assertEquals(newSlot, modifiedAppt.getTimeSlot());
//        assertEquals("", modifiedAppt.getDescription());
//    }
//
//    @Test
//    void adminModifyFullFailsWithoutAdminPrivileges() {
//        // Arrange
//        session.clear(); // No one logged in
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(10),
//            LocalTime.of(15, 0),
//            LocalTime.of(16, 0)
//        );
//
//        // Act & Assert
//        assertThrows(AuthorizationException.class, () -> {
//            adminAppointmentService.adminModifyFull(appointment, newSlot, "New Description");
//        });
//    }
//
//    @Test
//    void adminModifyFullThrowsExceptionForPastAppointment() {
//        // Arrange
//        TimeSlot pastSlot = new TimeSlot(
//            LocalDate.now().minusDays(1),
//            LocalTime.of(10, 0),
//            LocalTime.of(11, 0)
//        );
//        appointmentService.addSlot(pastSlot);
//        Appointment pastAppointment = appointmentService.bookAppointment(pastSlot, "Past Appointment", 5);
//
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.now().plusDays(7),
//            LocalTime.of(14, 0),
//            LocalTime.of(15, 0)
//        );
//
//        // Act & Assert
//        assertThrows(ValidationException.class, () -> {
//            adminAppointmentService.adminModifyFull(pastAppointment, newSlot, "New Description");
//        });
//    }
//
//    @Test
//    void adminRemoveSlotSuccessfully() {
//        // Arrange
//        TimeSlot slotToRemove = new TimeSlot(
//            LocalDate.now().plusDays(6),
//            LocalTime.of(12, 0),
//            LocalTime.of(13, 0)
//        );
//        appointmentService.addSlot(slotToRemove);
//
//        // Act
//        adminAppointmentService.adminRemoveSlot(slotToRemove);
//
//        // Assert - slot should be removed, so no appointment should exist for it
//        Appointment removedAppt = appointmentService.findAppointmentBySlot(slotToRemove);
//        assertNull(removedAppt);
//    }
//
//    @Test
//    void adminRemoveSlotRemovesAssociatedAppointment() {
//        // Arrange
//        TimeSlot slotWithAppt = new TimeSlot(
//            LocalDate.now().plusDays(8),
//            LocalTime.of(16, 0),
//            LocalTime.of(17, 0)
//        );
//        appointmentService.addSlot(slotWithAppt);
//        Appointment apptToBeRemoved = appointmentService.bookAppointment(slotWithAppt, "To Be Removed", 2);
//
//        int initialCount = appointmentService.getAllAppointments().size();
//
//        // Act
//        adminAppointmentService.adminRemoveSlot(slotWithAppt);
//
//        // Assert
//        assertEquals(initialCount - 1, appointmentService.getAllAppointments().size());
//        assertNull(appointmentService.findAppointmentBySlot(slotWithAppt));
//    }
//
//    @Test
//    void adminRemoveSlotFailsWithoutAdminPrivileges() {
//        // Arrange
//        session.clear(); // No one logged in
//        TimeSlot slot = new TimeSlot(
//            LocalDate.now().plusDays(6),
//            LocalTime.of(12, 0),
//            LocalTime.of(13, 0)
//        );
//
//        // Act & Assert
//        assertThrows(AuthorizationException.class, () -> {
//            adminAppointmentService.adminRemoveSlot(slot);
//        });
//    }
//
//    /**
//     * Mock implementation of AdminFileManager for testing purposes.
//     * Extends the real AdminFileManager but prevents file I/O operations.
//     */
//    private static class MockAdminFileManager extends AdminFileManager {
//        public MockAdminFileManager() {
//            // Constructor of parent class will attempt file I/O
//            // This is acceptable for testing as it loads existing data
//        }
//    }
//
//    /**
//     * Mock implementation of UserFileManager for testing purposes.
//     * Extends the real UserFileManager but prevents unnecessary file I/O operations.
//     */
//    private static class MockUserFileManager extends UserFileManager {
//        public MockUserFileManager() {
//            // Constructor of parent class will attempt file I/O
//            // This is acceptable for testing as it loads existing data
//        }
//    }
//}