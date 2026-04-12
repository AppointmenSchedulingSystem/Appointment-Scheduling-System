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

public class AppointmentService {
    private Schedule schedule;
    private List<Appointment> appointments;
    private NotificationService notificationService; ;
    private Session session;

    private AppointmentFileManager appointmentFileManager;


    // Default constructor
    public AppointmentService() {
        this.schedule = new Schedule(); // Initialize schedule
        this.appointments = new ArrayList<>();
    }
    public AppointmentService(Schedule schedule) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
    }
    // Constructor
    public AppointmentService(Schedule schedule, Session session, NotificationService notificationService) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
        this.session = session;
        this.notificationService  = notificationService;
    }

    // --- User methods ---
    public List<LocalDate> getAvailableDays() {
        return schedule.getAvailableDays();
    }

    /**
     * US1.3: return ONLY selectable slots (not fully booked).
     */
    public List<TimeSlot> getSlotsForDay(LocalDate date) {
        return getSlotsForDay(date, null);
    }

    public List<TimeSlot> getSlotsForDay(LocalDate date, AppointmentType type) {
        List<TimeSlot> slots = schedule.getAvailableSlotsForDay(date);
        List<TimeSlot> available = new ArrayList<>();

        BookingRuleStrategy strategy = (type != null)
                ? AppointmentTypeRuleFactory.getStrategy(type)
                : null;

        for (TimeSlot slot : slots) {
            Appointment a = findAppointmentBySlot(slot);
            if (a != null && a.isFull()) continue;

            // if a type was given, skip slots that violate its rules
            if (strategy != null) {
                long slotMinutes = slot.getDuration().toMinutes();
                if (slotMinutes > strategy.getMaxDurationMinutes()) continue;
                if (slot.getMaxCapacity() > strategy.getMaxCapacity()) continue;
            }

            available.add(slot);
        }

        return available;
    }

    public Appointment bookAppointment(TimeSlot slot, String description,
                                       int maxCapacity, AppointmentType type) {

        BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(type);

        // Build a temporary appointment to validate against strategy rules
        Appointment appointment = findAppointmentBySlot(slot);
        if (appointment == null) {
            String owner = (session != null && session.isUser())
                    ? session.getCurrentAccount().getUsername()
                    : "";
            appointment = new Appointment(slot, description, maxCapacity, type, owner);
            strategy.validate(appointment);// ← throws ValidationException if rules violated
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
     * Cancels ONE booking from an appointment.
     * If bookings drop to 0, removes the appointment record.
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

    // Modify appointment (same slot change)
    public Appointment modifyAppointment(Appointment oldAppt, TimeSlot newSlot) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now()))
            throw new ValidationException("Cannot modify a past appointment");

        AppointmentType type = oldAppt.getAppointmentType();   // ← preserve type
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, oldAppt.getDescription(), oldAppt.getMaxCapacity(), type);
    }

    // Modify appointment (slot + description)
    public Appointment modifyAppointmentFull(Appointment oldAppt, TimeSlot newSlot, String newDescription) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now()))
            throw new ValidationException("Cannot modify a past appointment");

        if (newDescription == null) newDescription = "";
        AppointmentType type = oldAppt.getAppointmentType();   // ← preserve type
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, newDescription, oldAppt.getMaxCapacity(), type);
    }

    // --- Utility ---
    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    public List<Appointment> getAppointmentsForUser(String username) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            if (username != null && username.equals(a.getOwnerUsername())) {
                result.add(a);
            }
        }
        return result;
    }

    Appointment findAppointmentBySlot(TimeSlot slot) {
        for (Appointment a : appointments) {
            if (a.getTimeSlot().equals(slot)) {
                return a;
            }
        }
        return null;
    }

    // --- Admin schedule management ---
    public void addSlot(TimeSlot slot) {
        schedule.addSlot(slot);
    }

    public void removeSlot(TimeSlot slot) {
        schedule.removeSlot(slot);

        Appointment apptToRemove = findAppointmentBySlot(slot);
        if (apptToRemove != null) {
            appointments.remove(apptToRemove);
        }
    }


    public void setAppointmentFileManager(AppointmentFileManager manager) {
        this.appointmentFileManager = manager;
    }

    public void loadAppointments() {
        if (appointmentFileManager != null) {
            this.appointments = appointmentFileManager.loadAppointmentsFromFile();
        }
    }

    public void saveAppointments() {
        if (appointmentFileManager != null) {
            appointmentFileManager.saveAppointmentsToFile(appointments);
        }
    }




}