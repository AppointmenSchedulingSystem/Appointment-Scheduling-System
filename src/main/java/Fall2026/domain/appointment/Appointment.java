// 📁 FILE: domain/appointment/Appointment.java

package Fall2026.domain.appointment;

import Fall2026.domain.exceptions.ValidationException;

public class Appointment {

    private final TimeSlot timeSlot;
    private final String description;
    private final int maxCapacity;
    private AppointmentStatus status;

    private int currentBookings;
    private String userEmail; // ✅ KEEP THIS

    public enum AppointmentStatus {
        CONFIRMED, PENDING, CANCELLED
    }

    public Appointment(TimeSlot timeSlot, String description, int maxCapacity) {
        if (timeSlot == null) throw new ValidationException("TimeSlot cannot be null.");
        if (description == null) description = "";
        if (maxCapacity <= 0) throw new ValidationException("maxCapacity must be positive.");

        this.timeSlot = timeSlot;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.currentBookings = 0;
        this.status = AppointmentStatus.CONFIRMED;
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

    public int getBookingsCount() {
        return currentBookings;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public boolean isFull() {
        return currentBookings >= maxCapacity;
    }

    public boolean addBooking() {
        if (isFull()) return false;
        currentBookings++;
        return true;
    }

    public void removeBooking() {
        if (currentBookings <= 0) {
            throw new ValidationException("Cannot remove booking: no bookings exist.");
        }
        currentBookings--;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    // ✅ EMAIL (used by NotificationService)
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}