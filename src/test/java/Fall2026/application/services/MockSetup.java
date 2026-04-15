package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * Reusable mock setup and test data factories for AdminAppointmentService tests.
 *
 * This class provides:
 * - Mock behavior setup helpers
 * - Test data factories
 * - Common test fixtures
 *
 * Usage in test classes:
 *   private MockSetup mockSetup;
 *
 *   @BeforeEach
 *   void setUp() {
 *       mockSetup = new MockSetup(authService, appointmentService);
 *       mockSetup.setupAuthorizedAdmin();
 *   }
 */
public class MockSetup {

    private final AuthService authService;
    private final AppointmentService appointmentService;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public MockSetup(AuthService authService, AppointmentService appointmentService) {
        this.authService = authService;
        this.appointmentService = appointmentService;
    }

    // ─── 1. Authorization Mock Behaviors ────────────────────────────────────

    /**
     * Setup: Admin is authorized
     * Simulates successful admin authorization check
     */
    public void setupAuthorizedAdmin() {
        doNothing().when(authService).requireAdmin();
    }

    /**
     * Setup: Admin is NOT authorized
     * Simulates failed authorization with custom message
     */
    public void setupUnauthorizedAdmin(String message) {
        doThrow(new AuthorizationException(message))
                .when(authService).requireAdmin();
    }

    /**
     * Setup: Admin is NOT authorized (default message)
     */
    public void setupUnauthorizedAdmin() {
        setupUnauthorizedAdmin("User is not an admin");
    }

    // ─── 2. Appointment Service Mock Behaviors ──────────────────────────────

    /**
     * Setup: Appointment cancellation succeeds
     */
    public void setupCancelAppointmentSuccess(Appointment appointment) {
        doNothing().when(appointmentService).cancelAppointment(appointment);
    }

    /**
     * Setup: Appointment cancellation fails with ValidationException
     */
    public void setupCancelAppointmentFails(Appointment appointment, String message) {
        doThrow(new ValidationException(message))
                .when(appointmentService).cancelAppointment(appointment);
    }

    /**
     * Setup: Appointment modification succeeds and returns modified appointment
     */
    public void setupModifyAppointmentSuccess(Appointment oldAppt, TimeSlot newSlot,
                                             Appointment resultAppt) {
        org.mockito.Mockito.when(appointmentService.modifyAppointment(oldAppt, newSlot))
                .thenReturn(resultAppt);
    }

    /**
     * Setup: Appointment modification fails with ValidationException
     */
    public void setupModifyAppointmentFails(Appointment oldAppt, TimeSlot newSlot, String message) {
        doThrow(new ValidationException(message))
                .when(appointmentService).modifyAppointment(oldAppt, newSlot);
    }

    /**
     * Setup: Full appointment modification succeeds
     */
    public void setupModifyAppointmentFullSuccess(Appointment oldAppt, TimeSlot newSlot,
                                                  String newDescription, Appointment resultAppt) {
        org.mockito.Mockito.when(appointmentService.modifyAppointmentFull(oldAppt, newSlot, newDescription))
                .thenReturn(resultAppt);
    }

    /**
     * Setup: Full appointment modification fails
     */
    public void setupModifyAppointmentFullFails(Appointment oldAppt, TimeSlot newSlot,
                                               String newDescription, String message) {
        doThrow(new ValidationException(message))
                .when(appointmentService).modifyAppointmentFull(oldAppt, newSlot, newDescription);
    }

    /**
     * Setup: Remove slot succeeds
     */
    public void setupRemoveSlotSuccess(TimeSlot slot) {
        doNothing().when(appointmentService).removeSlot(slot);
    }

    /**
     * Setup: Remove slot fails
     */
    public void setupRemoveSlotFails(TimeSlot slot, String message) {
        doThrow(new ValidationException(message))
                .when(appointmentService).removeSlot(slot);
    }

    // ─── 3. Test Data Factories ─────────────────────────────────────────────

    /**
     * Factory: Create a TimeSlot with flexible parameters
     *
     * Example:
     *   TimeSlot slot = mockSetup.createTimeSlot(LocalDate.now().plusDays(7), 10, 0, 11, 0, 5);
     */
    public TimeSlot createTimeSlot(LocalDate date, int startHour, int startMin,
                                   int endHour, int endMin, int capacity) {
        return new TimeSlot(
                date,
                LocalTime.of(startHour, startMin),
                LocalTime.of(endHour, endMin),
                capacity
        );
    }

    /**
     * Factory: Create a TimeSlot with defaults (tomorrow 10-11am, capacity 5)
     */
    public TimeSlot createTimeSlotDefault() {
        return createTimeSlot(LocalDate.now().plusDays(1), 10, 0, 11, 0, 5);
    }

    /**
     * Factory: Create a future TimeSlot (7 days from now)
     */
    public TimeSlot createFutureTimeSlot(int daysFromNow) {
        return createTimeSlot(LocalDate.now().plusDays(daysFromNow), 10, 0, 11, 0, 5);
    }

    /**
     * Factory: Create a past TimeSlot (for testing past appointment restrictions)
     */
    public TimeSlot createPastTimeSlot() {
        return createTimeSlot(LocalDate.now().minusDays(1), 10, 0, 11, 0, 5);
    }

    /**
     * Factory: Create an Appointment with all parameters customizable
     *
     * Example:
     *   Appointment appt = mockSetup.createAppointment(slot, "Checkup", 5, AppointmentType.IN_PERSON);
     */
    public Appointment createAppointment(TimeSlot slot, String description,
                                        int capacity, AppointmentType type) {
        return new Appointment(slot, description, capacity, type, "");
    }

    /**
     * Factory: Create default appointment (Regular Checkup, IN_PERSON)
     */
    public Appointment createAppointmentDefault(TimeSlot slot) {
        return createAppointment(slot, "Regular Checkup", 5, AppointmentType.IN_PERSON);
    }

    /**
     * Factory: Create pending appointment (for approval/rejection tests)
     */
    public Appointment createPendingAppointment(TimeSlot slot) {
        Appointment appt = createAppointmentDefault(slot);
        appt.setStatus(Appointment.AppointmentStatus.PENDING);
        return appt;
    }

    /**
     * Factory: Create confirmed appointment
     */
    public Appointment createConfirmedAppointment(TimeSlot slot) {
        Appointment appt = createAppointmentDefault(slot);
        appt.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return appt;
    }

    /**
     * Factory: Create cancelled appointment
     */
    public Appointment createCancelledAppointment(TimeSlot slot) {
        Appointment appt = createAppointmentDefault(slot);
        appt.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return appt;
    }

    /**
     * Factory: Create appointment with custom type
     */
    public Appointment createAppointmentWithType(TimeSlot slot, AppointmentType type) {
        return createAppointment(slot, type.name() + " Appointment", 5, type);
    }

    /**
     * Factory: Create appointment with custom description
     */
    public Appointment createAppointmentWithDescription(TimeSlot slot, String description) {
        return createAppointment(slot, description, 5, AppointmentType.IN_PERSON);
    }

    // ─── 4. Composite Setup Scenarios ───────────────────────────────────────

    /**
     * Complete scenario: Authorized admin, successful cancellation
     */
    public void setupSuccessfulCancelScenario(Appointment appointment) {
        setupAuthorizedAdmin();
        setupCancelAppointmentSuccess(appointment);
    }

    /**
     * Complete scenario: Unauthorized admin attempts cancellation
     */
    public void setupUnauthorizedCancelScenario(Appointment appointment) {
        setupUnauthorizedAdmin();
        // Don't setup appointmentService - should never be called
    }

    /**
     * Complete scenario: Authorized admin, modification succeeds
     */
    public void setupSuccessfulModifyScenario(Appointment oldAppt, TimeSlot newSlot,
                                             Appointment resultAppt) {
        setupAuthorizedAdmin();
        setupModifyAppointmentSuccess(oldAppt, newSlot, resultAppt);
    }

    /**
     * Complete scenario: Authorized admin approves pending appointment
     */
    public void setupSuccessfulApproveScenario(Appointment appointment) {
        setupAuthorizedAdmin();
        // Appointment status check happens inside the service method
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
    }

    /**
     * Complete scenario: Authorized admin rejects pending appointment
     */
    public void setupSuccessfulRejectScenario(Appointment appointment) {
        setupAuthorizedAdmin();
        setupCancelAppointmentSuccess(appointment);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
    }

    // ─── 5. Error Scenario Builders ──────────────────────────────────────────

    /**
     * Error scenario: Attempt to cancel past appointment
     */
    public void setupCannotCancelPastAppointmentScenario(Appointment pastAppointment) {
        setupAuthorizedAdmin();
        setupCancelAppointmentFails(pastAppointment, "Cannot cancel a past appointment.");
    }

    /**
     * Error scenario: Attempt to modify past appointment
     */
    public void setupCannotModifyPastAppointmentScenario(Appointment pastAppointment, TimeSlot newSlot) {
        setupAuthorizedAdmin();
        setupModifyAppointmentFails(pastAppointment, newSlot, "Cannot modify a past appointment");
    }

    /**
     * Error scenario: Attempt to approve non-pending appointment
     */
    public void setupCannotApproveNonPendingScenario(Appointment appointment,
                                                      Appointment.AppointmentStatus status) {
        setupAuthorizedAdmin();
        appointment.setStatus(status);
        // Service will throw ValidationException
    }

    /**
     * Error scenario: Slot not found when removing
     */
    public void setupSlotNotFoundScenario(TimeSlot slot) {
        setupAuthorizedAdmin();
        setupRemoveSlotFails(slot, "TimeSlot not found");
    }

    // ─── 6. Assertion Helpers ───────────────────────────────────────────────

    /**
     * Verify appointment status unchanged after failed operation
     */
    public boolean hasStatus(Appointment appointment, Appointment.AppointmentStatus expectedStatus) {
        return appointment.getStatus() == expectedStatus;
    }

    /**
     * Verify appointment matches expected properties
     */
    public boolean appointmentMatches(Appointment actual, Appointment expected) {
        return actual.getTimeSlot().equals(expected.getTimeSlot()) &&
               actual.getDescription().equals(expected.getDescription()) &&
               actual.getMaxCapacity() == expected.getMaxCapacity() &&
               actual.getAppointmentType() == expected.getAppointmentType();
    }
}

