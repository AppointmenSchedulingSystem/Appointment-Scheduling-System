package Fall2026.domain.appointment;

import Fall2026.domain.exceptions.ValidationException;

public class Appointment {
    private final TimeSlot timeSlot;       // the actual slot reserved
    private final String description;
    private final int maxCapacity;

    private int currentBookings;

    public Appointment(TimeSlot timeSlot, String description, int maxCapacity) {
        if (timeSlot == null) throw new ValidationException("TimeSlot cannot be null.");
        if (description == null) description = "";
        if (maxCapacity <= 0) throw new ValidationException("maxCapacity must be positive.");

        this.timeSlot = timeSlot;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.currentBookings = 0;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    // rename to match AppointmentService expectation
    public int getBookingsCount() {
        return currentBookings;
    }

    public int getCurrentBookings() {
        return currentBookings; // keep your old getter if other code uses it
    }

    public boolean isFull() {
        return currentBookings >= maxCapacity;
    }

    /** @return true if booking was added, false if already full */
    public boolean addBooking() {
        if (isFull()) return false;
        currentBookings++;
        return true;
    }

    /** Removes one booking. Throws if there are no bookings to remove. */
    public void removeBooking() {
        if (currentBookings <= 0) {
            throw new ValidationException("Cannot remove booking: no bookings exist.");
        }
        currentBookings--;
    }
}
