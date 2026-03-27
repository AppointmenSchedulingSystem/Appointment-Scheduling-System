package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//TODO: add ScheduleService same as AppointmentService but for Admin to manage the Schedule (add/remove slots, view all appointments, etc.)
public class AppointmentService {
    private Schedule schedule;
    private List<Appointment> appointments;
    private static final int MAX_DURATION_MINUTES = 120;

    public AppointmentService(Schedule schedule) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
    }

    //what the User sees before booking
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
            if (a == null) {
                // no bookings yet
                available.add(slot);
            } else if (!a.isFull()) {
                // partially booked
                available.add(slot);
            }
            // else: fully booked -> not shown
        }

        return available;
    }

    //        the core action

    public Appointment bookAppointment(TimeSlot slot, String description, int maxCapacity) {

        if (slot.getDuration().toMinutes() > MAX_DURATION_MINUTES) {
            throw new ValidationException("Duration exceeds 2 hour maximum");
        }

        Appointment appointment = findAppointmentBySlot(slot);

        // First booking for this slot: create the appointment record
        if (appointment == null) {
            appointment = new Appointment(slot, description, maxCapacity);
            appointments.add(appointment);
        }

        // Slot exists but is full
        if (appointment.isFull()) {
            throw new ValidationException("Sorry, that slot is fully booked.");
            //return null;
        }

        appointment.addBooking();
        //System.out.println("Booked: " + slot.getDate() + " at " + slot.getStartTime());
        return appointment;
    }

    // CANCELLATION — frees the slot back into Schedule

    /**
     * Cancels ONE booking from an appointment.
     * If bookings drop to 0, removes the appointment record.
     */
    public boolean cancelAppointment(Appointment appointment) {

        if (appointment.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot cancel a past appointment.");
            //return false;
        }

        // You need appointment.removeBooking() + getBookingsCount()
        appointment.removeBooking();

        if (appointment.getBookingsCount() <= 0) {
            appointments.remove(appointment);
        }

        //  System.out.println("Cancelled booking on " + appointment.getTimeSlot().getDate());
        return true;
    }

    /**
     * Modifies an existing appointment to a new time slot.
     * Validates that the appointment is not in the past, then cancels the old appointment
     * and books a new one with the same description and max capacity.
     * US4.1: Modify Appointment
     */
    public Appointment modifyAppointment(Appointment oldAppt, TimeSlot newSlot) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot modify a past appointment");
        }
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, oldAppt.getDescription(), oldAppt.getMaxCapacity());
    }

    /**
     * Modifies an existing appointment with new time slot and description.
     * US4.1: Modify Appointment (Enhanced)
     * Allows users to change the date, time, and description all at once.
     *
     * @param oldAppt        the appointment to modify
     * @param newSlot        the new time slot (date and time)
     * @param newDescription the new description
     * @return the modified appointment
     * @throws ValidationException if appointment is in the past or invalid slot
     */
    public Appointment modifyAppointmentFull(Appointment oldAppt, TimeSlot newSlot, String newDescription) {
        if (oldAppt.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot modify a past appointment");
        }
        if (newDescription == null) newDescription = "";
        cancelAppointment(oldAppt);
        return bookAppointment(newSlot, newDescription, oldAppt.getMaxCapacity());
    }

    // UTILITY
    public List<Appointment> getAllAppointments() {
        return appointments;
    }

    private Appointment findAppointmentBySlot(TimeSlot slot) {
        for (Appointment a : appointments) {
            if (a.getTimeSlot().equals(slot)) {
                return a;
            }
        }
        return null;
    }

    public void addSlot(TimeSlot slot) {
        schedule.addSlot(slot);
    }

    /**
     * Removes a time slot from the schedule.
     * US4.2: Admin can remove time slots
     *
     * @param slot the time slot to remove
     */
    public void removeSlot(TimeSlot slot) {
        schedule.removeSlot(slot);

        // Also remove any appointment associated with this slot
        Appointment apptToRemove = findAppointmentBySlot(slot);
        if (apptToRemove != null) {
            appointments.remove(apptToRemove);
        }
    }
}
