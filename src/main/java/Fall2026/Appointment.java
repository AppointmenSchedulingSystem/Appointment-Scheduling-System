package Fall2026;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Appointment {

    private LocalDateTime dateTime;
    private String description;
    private int maxCapacity;
    private int currentBookings;
    private boolean isAvailable;

    public Appointment(LocalDateTime dateTime, String description, int maxCapacity) {
        this.dateTime = dateTime;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.currentBookings = 0;
        this.isAvailable = true;
    }
    public static List<Appointment> getAvailableSlots(List<Appointment> allSlots) {
        List<Appointment> availableSlots = new ArrayList<>();
        for (Appointment slot : allSlots) {
            if (slot.isAvailable()) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }
public  boolean bookAppointment() {
    if (isAvailable) {
        currentBookings++;
        if (currentBookings >= maxCapacity) {
            isAvailable = false;
        }
        return true;
    }
    return false;
}

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
