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
    private final int maxCapacity;


    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime, int maxCapacity) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new ValidationException("End time must be after start time");
        }
        if (maxCapacity < 1) {
            throw new ValidationException("Max capacity must be at least 1");
        }

        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;

    }

    //fall back constructor with default max capacity of 1 --readd later

//    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
//        this(date, startTime, endTime, 1);
//    }


    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getMaxCapacity() { return maxCapacity; }


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
