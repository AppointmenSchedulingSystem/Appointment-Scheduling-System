package Fall2026.domain.appointment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Schedule {
    private List<TimeSlot> allSlots;

    public Schedule() {
        this.allSlots = new ArrayList<TimeSlot>();
    }
    public void addSlot(TimeSlot slot) {
        allSlots.add(slot);
    }
    public List<TimeSlot> getAvailableSlots() {
        return allSlots.stream()
                .filter(s -> !s.isBooked())
                .collect(Collectors.toList());
    }
    public List<TimeSlot> getAvailableSlotsForDay(LocalDate date) {
        return allSlots.stream()
                .filter(s -> !s.isBooked() && s.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<LocalDate> getAvailableDays() {
        return allSlots.stream()
                .filter(s -> !s.isBooked())
                .map(TimeSlot::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
