package Fall2026.domain.appointment;

import Fall2026.domain.exceptions.ValidationException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot {
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;



    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new ValidationException("End time must be after start time");
        }
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;

    }


    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }


    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(date, timeSlot.date)
                && Objects.equals(startTime, timeSlot.startTime)
                && Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime);
    }

    @Override
    public String toString() {
        return date + " " + startTime + "-" + endTime;
    }

}
