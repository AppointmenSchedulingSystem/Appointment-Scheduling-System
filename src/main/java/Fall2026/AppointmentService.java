package Fall2026;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private Schedule schedule;
    private List<Appointment> appointments;

    public AppointmentService(Schedule schedule) {
        this.schedule = schedule;
        this.appointments = new ArrayList<>();
    }

    //what the user sees before booking
    public List<LocalDate> getAvailableDays() {
        return schedule.getAvailableDays();
    }

    public List<TimeSlot> getSlotsForDay(LocalDate date) {
        return schedule.getAvailableSlotsForDay(date);
    }

        //        the core action

    public Appointment bookAppointment(TimeSlot slot, String description, int maxCapacity) {

        // try to lock the slot — fails if someone else just grabbed it
        if (!slot.markBooked()) {
            System.out.println("Sorry, that slot was just taken.");
            return null;
        }

        // create the appointment and store it
        Appointment appointment = new Appointment(slot, description, maxCapacity);
        appointment.addBooking();
        appointments.add(appointment);

        System.out.println("Booked: " + slot.getDate() + " at " + slot.getStartTime());
        return appointment;
    }

    // CANCELLATION — frees the slot back into Schedule

    public boolean cancelAppointment(Appointment appointment) {

        // only future appointments can be cancelled (Sprint 4 rule)
        if (appointment.getTimeSlot().getDate().isBefore(LocalDate.now())) {
            System.out.println("Cannot cancel a past appointment.");
            return false;
        }

        appointment.getTimeSlot().markAvailable(); // free the slot
        appointments.remove(appointment);
        System.out.println("Cancelled appointment on " + appointment.getTimeSlot().getDate());
        return true;
    }

    // UTILITY
    public List<Appointment> getAllAppointments() {
        return appointments;
    }


}
