package Fall2026;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlot {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBooked;

    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = false;
    }

    public boolean markBooked() {
        if (isBooked) return false;
        isBooked = true;
        return true;
    }

    public void markAvailable()        { this.isBooked = false; } // called on cancellation

    public LocalDate getDate()         { return date; }
    public LocalTime getStartTime()    { return startTime; }
    public LocalTime getEndTime()      { return endTime; }
    public boolean isBooked()          { return isBooked; }

}
