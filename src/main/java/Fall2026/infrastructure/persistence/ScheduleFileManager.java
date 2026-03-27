package Fall2026.infrastructure.persistence;

import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
                if (parts.length == 3) {
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    LocalTime startTime = LocalTime.parse(parts[1].trim()); // Parses HH:MM:SS format
                    LocalTime endTime = LocalTime.parse(parts[2].trim());   // Parses HH:MM:SS format

                    TimeSlot slot = new TimeSlot(date, startTime, endTime);
                    schedule.addSlot(slot);
                } else {
                    System.out.println("Skipping invalid slot line (expected 3 parts): " + line);
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
    private void saveSlotsToFile() {
        List<String> lines = new ArrayList<>();
        for (TimeSlot slot : schedule.getAllSlots()) {
            String line = slot.getDate() + "," + slot.getStartTime() + "," + slot.getEndTime();
            lines.add(line);
        }
        
        if (storage.WriteToFile(SLOTS_FILE, lines)) {
            System.out.println("Time slots saved to file successfully.");
        } else {
            System.out.println("Failed to save time slots to file.");
        }
    }
    
    /**
     * Add a new time slot and save to file
     */
    public void addTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        TimeSlot slot = new TimeSlot(date, startTime, endTime);
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
}

