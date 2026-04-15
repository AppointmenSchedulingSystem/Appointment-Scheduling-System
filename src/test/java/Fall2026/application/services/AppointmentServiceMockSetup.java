package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.account.User;
import Fall2026.infrastructure.notification.NotificationService;
import Fall2026.infrastructure.persistence.AppointmentFileManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Reusable mock setup and test data factories for AppointmentService tests.
 *
 * This class provides:
 * - Mock behavior setup helpers for all 4 dependencies
 * - Test data factories for TimeSlots, Appointments, and Users
 * - Scenario builders for common testing patterns
 *
 * Usage in test classes:
 * <pre>
 *   private AppointmentServiceMockSetup mockSetup;
 *
 *   @BeforeEach
 *   void setUp() {
 *       mockSetup = new AppointmentServiceMockSetup(schedule, session,
 *                                                     notificationService,
 *                                                     appointmentFileManager);
 *       mockSetup.setupNoUserLoggedIn();
 *   }
 * </pre>
 */
public class AppointmentServiceMockSetup {
    
    private final Schedule schedule;
    private final Session session;
    private final NotificationService notificationService;
    private final AppointmentFileManager appointmentFileManager;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public AppointmentServiceMockSetup(Schedule schedule, Session session,
                                       NotificationService notificationService,
                                       AppointmentFileManager appointmentFileManager) {
        this.schedule = schedule;
        this.session = session;
        this.notificationService = notificationService;
        this.appointmentFileManager = appointmentFileManager;
    }

    // ─── 1. Schedule Mock Behaviors ──────────────────────────────────────────

    /**
     * Setup: Schedule returns specific available days
     */
    public void setupAvailableDays(List<LocalDate> days) {
        when(schedule.getAvailableDays()).thenReturn(days);
    }

    /**
     * Setup: Schedule returns empty days list
     */
    public void setupNoDaysAvailable() {
        when(schedule.getAvailableDays()).thenReturn(new ArrayList<>());
    }

    /**
     * Setup: Schedule returns slots for a specific date
     */
    public void setupSlotsForDay(LocalDate date, List<TimeSlot> slots) {
        when(schedule.getAvailableSlotsForDay(date)).thenReturn(slots);
    }

    /**
     * Setup: Schedule returns no slots for a date
     */
    public void setupNoSlotsForDay(LocalDate date) {
        when(schedule.getAvailableSlotsForDay(date)).thenReturn(new ArrayList<>());
    }

    /**
     * Setup: Schedule remove slot succeeds
     */
    public void setupRemoveSlotSuccess(TimeSlot slot) {
        doNothing().when(schedule).removeSlot(slot);
    }

    // ─── 2. Session Mock Behaviors ──────────────────────────────────────────

    /**
     * Setup: No user logged in
     */
    public void setupNoUserLoggedIn() {
        when(session.isUser()).thenReturn(false);
    }

    /**
     * Setup: User is logged in
     */
    public void setupUserLoggedIn(User user) {
        when(session.isUser()).thenReturn(true);
        when(session.getCurrentAccount()).thenReturn(user);
    }

    /**
     * Setup: Specific user logged in (convenience method)
     */
    @SuppressWarnings("unused")
    public void setupUserLoggedInWithDefaults(int userId, String username) {
        User user = new User(userId, username, "password123", username + "@example.com");
        setupUserLoggedIn(user);
    }

    // ─── 3. NotificationService Mock Behaviors ──────────────────────────────

    /**
     * Setup: Notification service succeeds
     */
    public void setupNotificationSuccess() {
        doNothing().when(notificationService).notify(any(), any());
    }

    /**
     * Setup: Notification service fails
     */
    @SuppressWarnings("unused")
    public void setupNotificationFails(String message) {
        doThrow(new RuntimeException(message))
                .when(notificationService).notify(any(), any());
    }

    // ─── 4. AppointmentFileManager Mock Behaviors ────────────────────────────

    /**
     * Setup: File manager save succeeds
     */
    public void setupSaveSuccess() {
        doNothing().when(appointmentFileManager).saveAppointmentsToFile(any());
    }

    /**
     * Setup: File manager save fails
     */
    public void setupSaveFails(String message) {
        doThrow(new RuntimeException(message))
                .when(appointmentFileManager).saveAppointmentsToFile(any());
    }

    /**
     * Setup: File manager load returns appointments
     */
    public void setupLoadAppointments(List<Appointment> appointments) {
        when(appointmentFileManager.loadAppointmentsFromFile()).thenReturn(appointments);
    }

    /**
     * Setup: File manager load returns empty list
     */
    @SuppressWarnings("unused")
    public void setupLoadNoAppointments() {
        when(appointmentFileManager.loadAppointmentsFromFile()).thenReturn(new ArrayList<>());
    }

    // ─── 5. Test Data Factories ─────────────────────────────────────────────

    /**
     * Factory: Create a TimeSlot with custom parameters
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
     * Factory: Create a future TimeSlot (7 days from now)
     */
    public TimeSlot createFutureTimeSlot() {
        return createTimeSlot(LocalDate.now().plusDays(7), 10, 0, 11, 0, 1);
    }

    /**
     * Factory: Create a future TimeSlot with custom days offset
     */
    public TimeSlot createFutureTimeSlotWithDaysOffset(int daysOffset, @SuppressWarnings("unused") int capacity) {
        return createTimeSlot(LocalDate.now().plusDays(daysOffset), 10, 0, 11, 0, 1);
    }

    /**
     * Factory: Create a past TimeSlot (1 day ago)
     */
    public TimeSlot createPastTimeSlot() {
        return createTimeSlot(LocalDate.now().minusDays(1), 10, 0, 11, 0, 1);
    }

    /**
     * Factory: Create a fully booked TimeSlot
     */
    public TimeSlot createFullTimeSlot(int capacity) {
        return createTimeSlot(LocalDate.now().plusDays(7), 10, 0, 11, 0, capacity);
    }

    /**
     * Factory: Create an Appointment with custom parameters
     */
    public Appointment createAppointment(TimeSlot slot, String description,
                                        int capacity, AppointmentType type) {
        return new Appointment(slot, description, capacity, type, "");
    }

    /**
     * Factory: Create default appointment
     */
    public Appointment createDefaultAppointment(TimeSlot slot) {
        return createAppointment(slot, "Regular Checkup", 1, AppointmentType.IN_PERSON);
    }

    /**
     * Factory: Create appointment with custom type
     */
    public Appointment createAppointmentWithType(TimeSlot slot, AppointmentType type) {
        int capacity = getMaxCapacityForType(type);
        return createAppointment(slot, type.name() + " Appointment", capacity, type);
    }

    /**
     * Factory: Create appointment with custom description
     */
    public Appointment createAppointmentWithDescription(TimeSlot slot, String description) {
        return createAppointment(slot, description, 1, AppointmentType.IN_PERSON);
    }

    /**
     * Factory: Create appointment with 1 booking
     */
    public Appointment createBookedAppointment(TimeSlot slot) {
        Appointment appt = createDefaultAppointment(slot);
        appt.addBooking();
        return appt;
    }

    /**
     * Factory: Create appointment with multiple bookings
     */
    public Appointment createAppointmentWithBookings(TimeSlot slot, int bookingCount) {
        Appointment appt = createDefaultAppointment(slot);
        for (int i = 0; i < bookingCount; i++) {
            appt.addBooking();
        }
        return appt;
    }

    /**
     * Factory: Create fully booked appointment
     */
    public Appointment createFullyBookedAppointment(TimeSlot slot, int capacity) {
        Appointment appt = new Appointment(slot, "Full", capacity, AppointmentType.IN_PERSON, "");
        for (int i = 0; i < capacity; i++) {
            appt.addBooking();
        }
        return appt;
    }

    /**
     * Factory: Create a User
     */
    public User createUser(int id, String username, String email) {
        return new User(id, username, "password123", email);
    }

    /**
     * Factory: Create default test user
     */
    public User createDefaultUser() {
        return createUser(1, "testuser", "test@example.com");
    }

    // ─── 6. Composite Setup Scenarios ───────────────────────────────────────

    /**
     * Complete scenario: User logged in, notifications enabled, file save enabled
     */
    public void setupSuccessfulBookingScenario(User user) {
        setupUserLoggedIn(user);
        setupNotificationSuccess();
        setupSaveSuccess();
    }

    /**
     * Complete scenario: No user, no notifications, file save enabled
     */
    public void setupBookingWithoutUserScenario() {
        setupNoUserLoggedIn();
        setupSaveSuccess();
    }

    /**
     * Complete scenario: Schedule has slots, file manager works
     */
    @SuppressWarnings("unused")
    public void setupSuccessfulScheduleScenario(LocalDate date, List<TimeSlot> slots) {
        setupSlotsForDay(date, slots);
        setupSaveSuccess();
    }

    /**
     * Complete scenario: Load appointments from file succeeds
     */
    public void setupSuccessfulLoadScenario(List<Appointment> appointments) {
        setupLoadAppointments(appointments);
    }

    // ─── 7. Error Scenario Builders ──────────────────────────────────────────

    /**
     * Error scenario: Appointment is fully booked
     */
    public Appointment createFullyBookedScenario(@SuppressWarnings("unused") TimeSlot slot) {
        TimeSlot fullSlot = createFullTimeSlot(1);
        Appointment fullAppt = new Appointment(fullSlot, "Booked", 1, AppointmentType.IN_PERSON, "");
        fullAppt.addBooking();
        return fullAppt;
    }

    /**
     * Error scenario: Appointment is in the past
     */
    @SuppressWarnings("unused")
    public Appointment createPastAppointmentScenario() {
        TimeSlot pastSlot = createPastTimeSlot();
        return createDefaultAppointment(pastSlot);
    }

    /**
     * Error scenario: Try to cancel appointment with no bookings
     */
    public Appointment createEmptyBookingScenario(TimeSlot slot) {
        return createDefaultAppointment(slot); // 0 bookings by default
    }

    /**
     * Error scenario: File save fails
     */
    @SuppressWarnings("unused")
    public void setupSaveFailureScenario() {
        setupSaveFails("File write failed");
    }

    // ─── 8. Assertion Helpers ───────────────────────────────────────────────

    /**
     * Verify appointment has correct number of bookings
     */
    public boolean hasBookings(Appointment appointment, int expectedCount) {
        return appointment.getCurrentBookings() == expectedCount;
    }

    /**
     * Verify appointment has expected properties
     */
    @SuppressWarnings("unused")
    public boolean appointmentMatches(Appointment actual, TimeSlot expectedSlot,
                                      String expectedDescription, AppointmentType expectedType) {
        return actual.getTimeSlot().equals(expectedSlot) &&
               actual.getDescription().equals(expectedDescription) &&
               actual.getAppointmentType() == expectedType;
    }

    /**
     * Verify appointment is fully booked
     */
    @SuppressWarnings("unused")
    public boolean isFullyBooked(Appointment appointment) {
        return appointment.isFull();
    }

    /**
     * Create list of available days for testing
     */
    public List<LocalDate> createAvailableDaysList(int... daysOffset) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int offset : daysOffset) {
            days.add(today.plusDays(offset));
        }
        return days;
    }

    // ─── 9. Helper Method for Type-Appropriate Capacity ─────────────────────

    /**
     * Get max capacity for a given appointment type based on rule strategy
     */
    private int getMaxCapacityForType(AppointmentType type) {
        return switch (type) {
            case IN_PERSON, INDIVIDUAL, ASSESSMENT, FOLLOW_UP, VIRTUAL, URGENT -> 1;
            case GROUP -> 20;
        };
    }

    // ─── 10. Create list of TimeSlots for testing ────────────────────────────

    /**
     * Create list of TimeSlots for testing (uses parameter as capacity for each slot)
     */
    public List<TimeSlot> createTimeSlotList(LocalDate date, int... capacities) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        for (int i = 0; i < capacities.length; i++) {
            TimeSlot slot = createTimeSlot(
                    date,
                    startTime.getHour() + i,
                    startTime.getMinute(),
                    startTime.getHour() + i + 1,
                    startTime.getMinute(),
                    capacities[i]
            );
            slots.add(slot);
        }
        return slots;
    }
}

