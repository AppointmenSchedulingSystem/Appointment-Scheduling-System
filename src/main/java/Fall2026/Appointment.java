package Fall2026;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Appointment {

    private TimeSlot timeSlot;       // the actual slot reserved
    private String description;
    private int maxCapacity;
    private int currentBookings;
    private boolean isAvailable;

    public Appointment(TimeSlot timeSlot, String description, int maxCapacity) {
        this.timeSlot = timeSlot;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.currentBookings = 0;
        this.isAvailable = true;
    }
    public boolean addBooking() {
        if (!isAvailable) return false;
        currentBookings++;
        if (currentBookings >= maxCapacity) {
            isAvailable = false;
        }
        return true;
    }
    public TimeSlot getTimeSlot()      { return timeSlot; }
    public String getDescription()     { return description; }
    public int getMaxCapacity()        { return maxCapacity; }
    public int getCurrentBookings()    { return currentBookings; }
    public boolean isAvailable()       { return isAvailable; }
}
