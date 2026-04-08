package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import Fall2026.infrastructure.persistence.EmailService;
import Fall2026.domain.account.User;
import Fall2026.application.services.Session;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private Schedule schedule;
    private List<Appointment> appointments;
    private static final int MAX_DURATION_MINUTES = 120;
    private EmailService emailService;
    private Session session;

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
    public AppointmentService(Schedule schedule, Session session, EmailService emailService) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
        this.session = session;
        this.emailService = emailService;
    }

    // --- User methods ---
    public List<LocalDate> getAvailableDays() {
        return schedule.getAvailableDays();
    }

    /**
     * US1.3: return ONLY selectable slots (not fully booked).
     */
    public List<TimeSlot> getSlotsForDay(LocalDate date) {
        List<TimeSlot> slots = schedule.getAvailableSlotsForDay(date);
        List<TimeSlot> available = new ArrayList<>();

        for (TimeSlot slot : slots) {
            Appointment a = findAppointmentBySlot(slot);
            if (a == null || !a.isFull()) {
                available.add(slot);
            }
        }

        return available;
    }

    public Appointment bookAppointment(TimeSlot slot, String description, int maxCapacity) {
        // Existing booking logic...
        Appointment appointment = findAppointmentBySlot(slot);
        if (appointment == null) {
            appointment = new Appointment(slot, description, maxCapacity);
            appointments.add(appointment);
        }

        if (appointment.isFull()) {
            throw new ValidationException("Sorry, that slot is fully booked.");
        }

        appointment.addBooking();

        // --- SEND EMAIL TO GUEST ---
        if (session.isUser()) {
            User guest = (User) session.getCurrentAccount();
            emailService.sendGuestBookingEmail(
                    guest.getEmail(),
                    guest.getUsername(),
                    slot.getDate().toString(),
                    slot.getStartTime() + " → " + slot.getEndTime()
            );
        }

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

        return true;
    }

    // Modify appointment (same slot change)
    public Appointment modifyAppointment(Appointment oldAppt, TimeSlot newSlot) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot modify a past appointment");
        }
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, oldAppt.getDescription(), oldAppt.getMaxCapacity());
    }

    // Modify appointment (slot + description)
    public Appointment modifyAppointmentFull(Appointment oldAppt, TimeSlot newSlot, String newDescription) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot modify a past appointment");
        }
        if (newDescription == null) newDescription = "";
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, newDescription, oldAppt.getMaxCapacity());
    }

    // --- Utility ---
    public List<Appointment> getAllAppointments() {
        return appointments;
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





}