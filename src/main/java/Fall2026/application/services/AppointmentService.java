package Fall2026.application.services;

import Fall2026.application.rules.AppointmentTypeRuleFactory;
import Fall2026.application.rules.BookingRuleStrategy;
import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.notification.NotificationService;
import Fall2026.domain.account.User;
import Fall2026.application.services.Session;
import Fall2026.infrastructure.persistence.AppointmentFileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for managing appointment booking operations.
 *
 * <p>This class provides the core business logic for creating, modifying,
 * cancelling, and querying appointments. It integrates with the scheduling
 * system, session management, notification service, and file persistence.
 *
 * <p>Booking rules are enforced per {@link AppointmentType} via
 * {@link BookingRuleStrategy}, allowing each appointment type to define
 * its own capacity limits, duration constraints, and auto-approval behaviour.
 *
 * @author Mohannad Hamad
 * @see Appointment
 * @see Schedule
 * @see AppointmentTypeRuleFactory
 */
public class AppointmentService {

    /** The schedule that holds all available time slots. */
    private Schedule schedule;

    /** In-memory list of all currently booked appointments. */
    private List<Appointment> appointments;

    /** Service used to send notifications to users after booking actions. */
    private NotificationService notificationService;

    /** The current authenticated session, used to identify the acting user. */
    private Session session;

    /** File manager responsible for persisting and loading appointments. */
    private AppointmentFileManager appointmentFileManager;



    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor. Initializes a fresh {@link Schedule} and an empty
     * appointments list. Suitable for lightweight or test contexts where no
     * session or notification service is needed.
     */
    public AppointmentService() {
        this.schedule = new Schedule();
        this.appointments = new ArrayList<>();
    }

    /**
     * Constructs an {@code AppointmentService} with a pre-existing schedule.
     *
     * @param schedule the schedule containing available time slots; must not be {@code null}
     */
    public AppointmentService(Schedule schedule) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
    }

    /**
     * Constructs a fully-configured {@code AppointmentService} with all dependencies.
     *
     * @param schedule            the schedule containing available time slots; must not be {@code null}
     * @param session             the current user session used to retrieve the logged-in account
     * @param notificationService the service used to notify users after booking actions
     */
    public AppointmentService(Schedule schedule, Session session, NotificationService notificationService) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
        this.session = session;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    /**
     * Returns all dates that have at least one available time slot.
     *
     * @return a list of {@link LocalDate} objects representing days with open slots;
     *         never {@code null}, may be empty
     */
    public List<LocalDate> getAvailableDays() {
        return schedule.getAvailableDays();
    }

    /**
     * Returns all selectable (not fully booked) time slots for the given date,
     * without filtering by appointment type.
     *
     * <p>US1.3: Only slots that still have capacity are returned.
     *
     * @param date the date to query; must not be {@code null}
     * @return a list of available {@link TimeSlot} objects for that date;
     *         never {@code null}, may be empty
     */
    public List<TimeSlot> getSlotsForDay(LocalDate date) {
        return getSlotsForDay(date, null);
    }

    /**
     * Returns selectable time slots for the given date, optionally filtered
     * by the rules of the specified appointment type.
     *
     * <p>US1.3: Fully booked slots are always excluded. When {@code type} is
     * provided, slots that exceed the type's maximum duration or maximum capacity
     * are also excluded.
     *
     * @param date the date to query; must not be {@code null}
     * @param type the appointment type whose booking rules are used to filter slots,
     *             or {@code null} to skip type-based filtering
     * @return a filtered list of available {@link TimeSlot} objects; never {@code null}
     */
    public List<TimeSlot> getSlotsForDay(LocalDate date, AppointmentType type) {
        List<TimeSlot> slots = schedule.getAvailableSlotsForDay(date);
        List<TimeSlot> available = new ArrayList<>();

        BookingRuleStrategy strategy = (type != null)
                ? AppointmentTypeRuleFactory.getStrategy(type)
                : null;

        for (TimeSlot slot : slots) {
            Appointment a = findAppointmentBySlot(slot);
            if (a != null && a.isFull()) continue;

            if (strategy != null) {
                long slotMinutes = slot.getDuration().toMinutes();
                if (slotMinutes > strategy.getMaxDurationMinutes()) continue;
                if (slot.getMaxCapacity() > strategy.getMaxCapacity()) continue;
            }

            available.add(slot);
        }

        return available;
    }

    // -------------------------------------------------------------------------
    // Booking operations
    // -------------------------------------------------------------------------

    /**
     * Books an appointment into the given time slot.
     *
     * <p>If no appointment record exists for the slot yet, one is created and
     * validated against the {@link BookingRuleStrategy} for the given type.
     * The initial status is set to {@link Appointment.AppointmentStatus#CONFIRMED}
     * if the strategy auto-approves, or {@link Appointment.AppointmentStatus#PENDING}
     * otherwise. Each call increments the booking count by one.
     *
     * <p>If a user session is active, a notification with the booking details
     * and status is sent to the logged-in user.
     *
     * @param slot        the time slot to book; must not be {@code null}
     * @param description a short description or reason for the appointment
     * @param maxCapacity the maximum number of concurrent bookings allowed for the slot
     * @param type        the type of appointment being booked; must not be {@code null}
     * @return the created or updated {@link Appointment}
     * @throws ValidationException if the booking violates the type's rules,
     *                             or if the slot is already fully booked
     */
    public Appointment bookAppointment(TimeSlot slot, String description,
                                       int maxCapacity, AppointmentType type) {

        BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(type);

        Appointment appointment = findAppointmentBySlot(slot);
        if (appointment == null) {
            String owner = (session != null && session.isUser())
                    ? session.getCurrentAccount().getUsername()
                    : "";
            appointment = new Appointment(slot, description, maxCapacity, type, owner);
            strategy.validate(appointment);
            if (strategy.isAutoApproved()) {
                appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            } else {
                appointment.setStatus(Appointment.AppointmentStatus.PENDING);
            }
            appointments.add(appointment);
        }

        if (appointment.isFull()) {
            throw new ValidationException("Sorry, that slot is fully booked.");
        }

        appointment.addBooking();

        if (session != null && session.isUser()) {
            User guest = (User) session.getCurrentAccount();
            String statusNote = strategy.isAutoApproved()
                    ? "Your appointment has been <b>confirmed</b>."
                    : "Your appointment has been <b>submitted and is awaiting admin approval</b>.";
            String message = statusNote + "<br><br>"
                    + "<b>📅 Date:</b> " + slot.getDate() + "<br>"
                    + "<b>⏰ Time:</b> " + slot.getStartTime() + " → " + slot.getEndTime() + "<br>"
                    + "<b>📋 Type:</b> " + type.name();
            notificationService.notify(guest, message);
        }

        saveAppointments();
        return appointment;
    }

    /**
     * Cancels one booking from an appointment.
     *
     * <p>Decrements the booking count by one. If the count reaches zero,
     * the appointment record is removed from the in-memory list entirely.
     * Changes are persisted via {@link #saveAppointments()}.
     *
     * @param appointment the appointment to cancel; must not be {@code null}
     * @return {@code true} if the cancellation succeeded
     * @throws ValidationException if the appointment's date is in the past
     */
    public boolean cancelAppointment(Appointment appointment) {
        if (appointment.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot cancel a past appointment.");
        }

        appointment.removeBooking();

        if (appointment.getBookingsCount() <= 0) {
            appointments.remove(appointment);
        }
        saveAppointments();
        return true;
    }

    /**
     * Moves an appointment to a new time slot, preserving its type and description.
     *
     * <p>The original appointment is cancelled and a new booking is created in
     * {@code newSlot} using the same {@link AppointmentType}, description, and
     * max capacity as the original.
     *
     * @param oldAppt the appointment to move; must not be {@code null}
     * @param newSlot the replacement time slot; must not be {@code null}
     * @return the newly created {@link Appointment} in {@code newSlot}
     * @throws ValidationException if {@code oldAppt} is in the past, or if
     *                             {@code newSlot} is full or violates booking rules
     */
    public Appointment modifyAppointment(Appointment oldAppt, TimeSlot newSlot) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now()))
            throw new ValidationException("Cannot modify a past appointment");

        AppointmentType type = oldAppt.getAppointmentType();
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, oldAppt.getDescription(), oldAppt.getMaxCapacity(), type);
    }

    /**
     * Moves an appointment to a new time slot and updates its description,
     * preserving the original appointment type and max capacity.
     *
     * <p>The original appointment is cancelled and re-booked in {@code newSlot}
     * with {@code newDescription}. If {@code newDescription} is {@code null},
     * an empty string is used.
     *
     * @param oldAppt        the appointment to modify; must not be {@code null}
     * @param newSlot        the replacement time slot; must not be {@code null}
     * @param newDescription the updated description/reason; {@code null} is treated as {@code ""}
     * @return the newly created {@link Appointment} in {@code newSlot}
     * @throws ValidationException if {@code oldAppt} is in the past, or if
     *                             {@code newSlot} is full or violates booking rules
     */
    public Appointment modifyAppointmentFull(Appointment oldAppt, TimeSlot newSlot, String newDescription) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now()))
            throw new ValidationException("Cannot modify a past appointment");

        if (newDescription == null) newDescription = "";
        AppointmentType type = oldAppt.getAppointmentType();
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, newDescription, oldAppt.getMaxCapacity(), type);
    }

    // -------------------------------------------------------------------------
    // Utility / accessors
    // -------------------------------------------------------------------------

    /**
     * Returns all appointments currently held in memory.
     *
     * @return a mutable list of all {@link Appointment} records; never {@code null}
     */
    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    /**
     * Finds the appointment associated with the given time slot.
     *
     * @param slot the time slot to look up; must not be {@code null}
     * @return the matching {@link Appointment}, or {@code null} if none exists
     */
    Appointment findAppointmentBySlot(TimeSlot slot) {
        for (Appointment a : appointments) {
            if (a.getTimeSlot().equals(slot)) {
                return a;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Admin schedule management
    // -------------------------------------------------------------------------

    /**
     * Adds a new time slot to the schedule, making it available for booking.
     *
     * @param slot the time slot to add; must not be {@code null}
     */
    public void addSlot(TimeSlot slot) {
        schedule.addSlot(slot);
    }

    /**
     * Removes a time slot from the schedule and cancels any appointment booked into it.
     *
     * <p>If an appointment exists for the given slot it is removed from the
     * in-memory list directly, bypassing normal cancellation validation
     * (e.g. past-date checks do not apply here).
     *
     * @param slot the time slot to remove; must not be {@code null}
     */
    public void removeSlot(TimeSlot slot) {
        schedule.removeSlot(slot);

        Appointment apptToRemove = findAppointmentBySlot(slot);
        if (apptToRemove != null) {
            appointments.remove(apptToRemove);
        }
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /**
     * Sets the file manager used to persist and load appointments.
     *
     * <p>Must be called before {@link #loadAppointments()} or
     * {@link #saveAppointments()} if file persistence is required.
     *
     * @param manager the {@link AppointmentFileManager} to use; passing {@code null}
     *                disables file persistence
     */
    public void setAppointmentFileManager(AppointmentFileManager manager) {
        this.appointmentFileManager = manager;
    }

    /**
     * Loads appointments from the file system into memory, replacing the current
     * in-memory list.
     *
     * <p>Has no effect if no {@link AppointmentFileManager} has been set via
     * {@link #setAppointmentFileManager(AppointmentFileManager)}.
     */
    public void loadAppointments() {
        if (appointmentFileManager != null) {
            this.appointments = appointmentFileManager.loadAppointmentsFromFile();
        }
    }

    /**
     * Persists the current in-memory appointment list to the file system.
     *
     * <p>Has no effect if no {@link AppointmentFileManager} has been set via
     * {@link #setAppointmentFileManager(AppointmentFileManager)}.
     */
    public void saveAppointments() {
        if (appointmentFileManager != null) {
            appointmentFileManager.saveAppointmentsToFile(appointments);
        }
    }

    public List<Appointment> getAppointmentsForUser(String username) {
        return appointments.stream()
                .filter(a -> username.equals(a.getOwnerUsername()))
                .collect(Collectors.toList());
    }


}