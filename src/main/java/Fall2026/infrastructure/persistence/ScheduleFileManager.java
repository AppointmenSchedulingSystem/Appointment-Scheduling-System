package Fall2026.infrastructure.persistence;

import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduleFileManager {
    private static final String SLOTS_FILE = "Slots.txt";
    
    private final Schedule schedule;
    private final CredentialStorage storage;
    
    public ScheduleFileManager() {
        this.storage = new CredentialStorage();
        this.schedule = new Schedule();
        loadSlotsFromFile();
    }

    /**
     * Constructor that accepts an existing Schedule object and loads slots into it.
     * Used by App.java to load slots into the shared Schedule instance.
     */
    public ScheduleFileManager(Schedule existingSchedule) {
        this.storage = new CredentialStorage();
        this.schedule = existingSchedule;
        loadSlotsFromFile();
    }
    
    /**
     * Load time slots from Slots.txt on startup
     */
    private void loadSlotsFromFile() {
        List<String> lines = storage.ReadFromFile(SLOTS_FILE);
        if (lines.isEmpty()) {
            System.out.println("No time slots found in file: " + SLOTS_FILE);
            return;
        }
        
        for (String line : lines) {
            try {
                String[] parts = line.split(",");
                // expected format: date,startTime,endTime (times include seconds HH:MM:SS)
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    LocalTime startTime = LocalTime.parse(parts[1].trim());
                    LocalTime endTime = LocalTime.parse(parts[2].trim());
                    int maxCapacity = Integer.parseInt(parts[3].trim());
                    TimeSlot slot = new TimeSlot(date, startTime, endTime, maxCapacity);
                    schedule.addSlot(slot);
                } else {
                    System.out.println("Skipping invalid slot line (expected 4 parts): " + line);
                }
            } catch (Exception e) {
                System.out.println("Error parsing slot line '" + line + "': " + e.getMessage());
            }
        }
        
        System.out.println("Loaded " + schedule.getAllSlots().size() + " time slots from file.");
    }
    
    /**
     * Save all time slots to Slots.txt in format: date,startTime(HH:MM:SS),endTime(HH:MM:SS)
     */
    public void saveSlotsToFile() {
        List<String> lines = new ArrayList<>();
        for (TimeSlot slot : schedule.getAllSlots()) {
            String line = slot.getDate() + "," + slot.getStartTime() + "," + slot.getEndTime() + "," + slot.getMaxCapacity();
            lines.add(line);
        }
        
        if (storage.WriteToFile(SLOTS_FILE, lines)) {
            // File saved successfully (don't print - let caller handle messages)
        } else {
            System.out.println("Failed to save time slots to file.");
        }
    }
    
    /**
     * Add a new time slot and save to file
     */
    public void addTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime, int maxCapacity) {
        TimeSlot slot = new TimeSlot(date, startTime, endTime, maxCapacity);
            schedule.addSlot(slot);
        saveSlotsToFile();
        System.out.println("New time slot added: " + date + " from " + startTime + " to " + endTime);
    }
    
    /**
     * Add a time slot object and save to file
     */
    public void addTimeSlot(TimeSlot slot) {
        schedule.addSlot(slot);
        saveSlotsToFile();
        System.out.println("New time slot added: " + slot.getDate() + " from " + slot.getStartTime() + " to " + slot.getEndTime());
    }
    
    /**
     * Check if a time slot already exists (duplicate detection)
     * @param slot the time slot to check
     * @return true if slot exists, false otherwise
     */
    public boolean slotExists(TimeSlot slot) {
        for (TimeSlot existing : schedule.getAllSlots()) {
            if (existing.equals(slot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a time slot conflicts with existing slots (overlapping times on same date)
     * Detects:
     * - Exact duplicates (same start and end time)
     * - Overlapping slots (new slot starts before existing slot ends)
     * - Touching starts (slots that start at same time)
     * 
     * @param slot the time slot to check
     * @return true if a conflict exists, false otherwise
     */
    public boolean hasTimeConflict(TimeSlot slot) {
        for (TimeSlot existing : schedule.getAllSlots()) {
            // Skip self-comparison to allow checking slot against itself (not a conflict if already added)
            if (existing.equals(slot)) {
                continue;
            }
            // Only check slots on the same date
            if (existing.getDate().equals(slot.getDate())) {
                // Check for time range overlap:
                // Conflict exists if: newSlot.start < existing.end AND newSlot.end > existing.start
                if (slot.getStartTime().isBefore(existing.getEndTime()) && 
                    slot.getEndTime().isAfter(existing.getStartTime())) {
                    return true;
                }
                // Also check for exact same start time (even if end times differ)
                if (slot.getStartTime().equals(existing.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the schedule object
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Get all time slots
     */
    public List<TimeSlot> getAllSlots() {
        return schedule.getAllSlots();
    }

    public Map<Object, Object> getAvailableSlots() {
        return Map.of();
    }
}

