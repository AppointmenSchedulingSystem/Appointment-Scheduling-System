package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAppointmentServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AuthService authService;

    private AdminAppointmentService adminAppointmentService;

    private TimeSlot testTimeSlot;
    private Appointment testAppointment;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        adminAppointmentService = new AdminAppointmentService(appointmentService, authService);

        // Create test fixtures
        testTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                1
        );

        testAppointment = new Appointment(
                testTimeSlot,
                "Regular Checkup",
                1,
                AppointmentType.IN_PERSON
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // ========== adminCancel() Tests ==========

    @Test
    void adminCancel_shouldCancelAppointment_whenAdminIsAuthorized() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        when(appointmentService.cancelAppointment(testAppointment)).thenReturn(true);

        // Act
        adminAppointmentService.adminCancel(testAppointment);

        // Assert
        verify(authService).requireAdmin();
        verify(appointmentService).cancelAppointment(testAppointment);
    }

    @Test
    void adminCancel_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminCancel(testAppointment)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).cancelAppointment(any());
    }

    @Test
    void adminCancel_shouldNotCancelAppointment_whenValidationExceptionOccurs() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        doThrow(new ValidationException("Cannot cancel a past appointment"))
                .when(appointmentService).cancelAppointment(testAppointment);

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminCancel(testAppointment)
        );

        verify(authService).requireAdmin();
        verify(appointmentService).cancelAppointment(testAppointment);
    }

    @Test
    void adminCancel_shouldHandleNullAppointment() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        doThrow(new NullPointerException())
                .when(appointmentService).cancelAppointment(any());

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> adminAppointmentService.adminCancel(null)
        );

        verify(authService).requireAdmin();
    }

    // ========== adminModify() Tests ==========

    @Test
    void adminModify_shouldModifyAppointment_whenAdminIsAuthorized() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        Appointment modifiedAppointment = new Appointment(
                newTimeSlot,
                "Regular Checkup",
                1,
                AppointmentType.IN_PERSON
        );

        doNothing().when(authService).requireAdmin();
        when(appointmentService.modifyAppointment(testAppointment, newTimeSlot))
                .thenReturn(modifiedAppointment);

        // Act
        Appointment result = adminAppointmentService.adminModify(testAppointment, newTimeSlot);

        // Assert
        assertNotNull(result);
        assertEquals(newTimeSlot, result.getTimeSlot());
        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointment(testAppointment, newTimeSlot);
    }

    @Test
    void adminModify_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminModify(testAppointment, newTimeSlot)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).modifyAppointment(any(), any());
    }

    @Test
    void adminModify_shouldThrowValidationException_whenAppointmentCannotBeModified() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().minusDays(1),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        doNothing().when(authService).requireAdmin();
        doThrow(new ValidationException("Cannot modify a past appointment"))
                .when(appointmentService).modifyAppointment(testAppointment, newTimeSlot);

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminModify(testAppointment, newTimeSlot)
        );

        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointment(testAppointment, newTimeSlot);
    }

    @Test
    void adminModify_shouldReturnModifiedAppointment_withDifferentDescription() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        Appointment modifiedAppointment = new Appointment(
                newTimeSlot,
                "Updated Description",
                1,
                AppointmentType.IN_PERSON
        );

        doNothing().when(authService).requireAdmin();
        when(appointmentService.modifyAppointment(testAppointment, newTimeSlot))
                .thenReturn(modifiedAppointment);

        // Act
        Appointment result = adminAppointmentService.adminModify(testAppointment, newTimeSlot);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Description", result.getDescription());
        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointment(testAppointment, newTimeSlot);
    }

    // ========== adminModifyFull() Tests ==========

    @Test
    void adminModifyFull_shouldModifyAppointmentWithNewSlotAndDescription_whenAdminIsAuthorized() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        String newDescription = "Follow-up Consultation";
        Appointment modifiedAppointment = new Appointment(
                newTimeSlot,
                newDescription,
                1,
                AppointmentType.FOLLOW_UP
        );

        doNothing().when(authService).requireAdmin();
        when(appointmentService.modifyAppointmentFull(testAppointment, newTimeSlot, newDescription))
                .thenReturn(modifiedAppointment);

        // Act
        Appointment result = adminAppointmentService.adminModifyFull(testAppointment, newTimeSlot, newDescription);

        // Assert
        assertNotNull(result);
        assertEquals(newTimeSlot, result.getTimeSlot());
        assertEquals(newDescription, result.getDescription());
        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointmentFull(testAppointment, newTimeSlot, newDescription);
    }

    @Test
    void adminModifyFull_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminModifyFull(testAppointment, newTimeSlot, "New Description")
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).modifyAppointmentFull(any(), any(), any());
    }

    @Test
    void adminModifyFull_shouldThrowValidationException_whenAppointmentCannotBeModified() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().minusDays(1),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        doNothing().when(authService).requireAdmin();
        doThrow(new ValidationException("Cannot modify a past appointment"))
                .when(appointmentService).modifyAppointmentFull(testAppointment, newTimeSlot, "New Description");

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminModifyFull(testAppointment, newTimeSlot, "New Description")
        );

        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointmentFull(testAppointment, newTimeSlot, "New Description");
    }

    @Test
    void adminModifyFull_shouldModifyAppointmentWithEmptyDescription() {
        // Arrange
        TimeSlot newTimeSlot = new TimeSlot(
                LocalDate.now().plusDays(14),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                1
        );
        Appointment modifiedAppointment = new Appointment(
                newTimeSlot,
                "",
                1,
                AppointmentType.IN_PERSON
        );

        doNothing().when(authService).requireAdmin();
        when(appointmentService.modifyAppointmentFull(testAppointment, newTimeSlot, ""))
                .thenReturn(modifiedAppointment);

        // Act
        Appointment result = adminAppointmentService.adminModifyFull(testAppointment, newTimeSlot, "");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDescription());
        verify(authService).requireAdmin();
        verify(appointmentService).modifyAppointmentFull(testAppointment, newTimeSlot, "");
    }

    // ========== adminRemoveSlot() Tests ==========

    @Test
    void adminRemoveSlot_shouldRemoveSlot_whenAdminIsAuthorized() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        doNothing().when(appointmentService).removeSlot(testTimeSlot);

        // Act
        adminAppointmentService.adminRemoveSlot(testTimeSlot);

        // Assert
        verify(authService).requireAdmin();
        verify(appointmentService).removeSlot(testTimeSlot);
    }

    @Test
    void adminRemoveSlot_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminRemoveSlot(testTimeSlot)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).removeSlot(any());
    }

    @Test
    void adminRemoveSlot_shouldThrowValidationException_whenSlotDoesNotExist() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        doThrow(new ValidationException("TimeSlot not found"))
                .when(appointmentService).removeSlot(testTimeSlot);

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminRemoveSlot(testTimeSlot)
        );

        verify(authService).requireAdmin();
        verify(appointmentService).removeSlot(testTimeSlot);
    }

    @Test
    void adminRemoveSlot_shouldHandleNullTimeSlot() {
        // Arrange
        doNothing().when(authService).requireAdmin();
        doThrow(new NullPointerException())
                .when(appointmentService).removeSlot(any());

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> adminAppointmentService.adminRemoveSlot(null)
        );

        verify(authService).requireAdmin();
    }

    // ========== adminApprove() Tests ==========

    @Test
    void adminApprove_shouldApproveAppointment_whenAppointmentIsPending() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doNothing().when(authService).requireAdmin();

        // Act
        Appointment result = adminAppointmentService.adminApprove(testAppointment);

        // Assert
        assertNotNull(result);
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, result.getStatus());
        verify(authService).requireAdmin();
    }

    @Test
    void adminApprove_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminApprove(testAppointment)
        );

        verify(authService).requireAdmin();
        assertEquals(Appointment.AppointmentStatus.PENDING, testAppointment.getStatus());
    }

    @Test
    void adminApprove_shouldThrowValidationException_whenAppointmentIsNotPending() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        doNothing().when(authService).requireAdmin();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminApprove(testAppointment)
        );

        verify(authService).requireAdmin();
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, testAppointment.getStatus());
    }

    @Test
    void adminApprove_shouldThrowValidationException_whenAppointmentIsCancelled() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        doNothing().when(authService).requireAdmin();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminApprove(testAppointment)
        );

        verify(authService).requireAdmin();
        assertEquals(Appointment.AppointmentStatus.CANCELLED, testAppointment.getStatus());
    }

    @Test
    void adminApprove_shouldReturnAppointmentWithConfirmedStatus() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doNothing().when(authService).requireAdmin();

        // Act
        Appointment result = adminAppointmentService.adminApprove(testAppointment);

        // Assert
        assertSame(testAppointment, result);
        assertEquals(Appointment.AppointmentStatus.CONFIRMED, result.getStatus());
        assertEquals(testAppointment.getDescription(), result.getDescription());
        assertEquals(testAppointment.getAppointmentType(), result.getAppointmentType());
    }

    // ========== adminReject() Tests ==========

    @Test
    void adminReject_shouldRejectAppointment_whenAppointmentIsPending() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doNothing().when(authService).requireAdmin();
        when(appointmentService.cancelAppointment(testAppointment)).thenReturn(true);

        // Act
        adminAppointmentService.adminReject(testAppointment);

        // Assert
        verify(authService).requireAdmin();
        verify(appointmentService).cancelAppointment(testAppointment);
    }

    @Test
    void adminReject_shouldThrowAuthorizationException_whenAdminIsNotAuthorized() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doThrow(new AuthorizationException("User is not an admin"))
                .when(authService).requireAdmin();

        // Act & Assert
        assertThrows(AuthorizationException.class,
                () -> adminAppointmentService.adminReject(testAppointment)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).cancelAppointment(any());
    }

    @Test
    void adminReject_shouldThrowValidationException_whenAppointmentIsNotPending() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        doNothing().when(authService).requireAdmin();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminReject(testAppointment)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).cancelAppointment(any());
    }

    @Test
    void adminReject_shouldThrowValidationException_whenAppointmentIsCancelled() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        doNothing().when(authService).requireAdmin();

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> adminAppointmentService.adminReject(testAppointment)
        );

        verify(authService).requireAdmin();
        verify(appointmentService, never()).cancelAppointment(any());
    }

    @Test
    void adminReject_shouldCancelAppointmentOnce() {
        // Arrange
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        doNothing().when(authService).requireAdmin();
        when(appointmentService.cancelAppointment(testAppointment)).thenReturn(true);

        // Act
        adminAppointmentService.adminReject(testAppointment);

        // Assert
        verify(authService, times(1)).requireAdmin();
        verify(appointmentService, times(1)).cancelAppointment(testAppointment);
    }
}