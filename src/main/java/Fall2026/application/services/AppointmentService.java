package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//TODO: add ScheduleService same as AppointmentService but for Admin to manage the Schedule (add/remove slots, view all appointments, etc.)
public class AppointmentService {
    private Schedule schedule;
    private List<Appointment> appointments;

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

        Appointment appointment = findAppointmentBySlot(slot);

        // First booking for this slot: create the appointment record
        if (appointment == null) {
            appointment = new Appointment(slot, description, maxCapacity);
            appointments.add(appointment);
        }

        // Slot exists but is full
        if (appointment.isFull()) {
            System.out.println("Sorry, that slot is fully booked.");
            return null;
        }

        appointment.addBooking();
        System.out.println("Booked: " + slot.getDate() + " at " + slot.getStartTime());
        return appointment;
    }

    // CANCELLATION — frees the slot back into Schedule

    /**
     * Cancels ONE booking from an appointment.
     * If bookings drop to 0, removes the appointment record.
     */
    public boolean cancelAppointment(Appointment appointment) {

        if (appointment.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            System.out.println("Cannot cancel a past appointment.");
            return false;
        }

        // You need appointment.removeBooking() + getBookingsCount()
        appointment.removeBooking();

        if (appointment.getBookingsCount() <= 0) {
            appointments.remove(appointment);
        }

        System.out.println("Cancelled booking on " + appointment.getTimeSlot().getDate());
        return true;
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


}
