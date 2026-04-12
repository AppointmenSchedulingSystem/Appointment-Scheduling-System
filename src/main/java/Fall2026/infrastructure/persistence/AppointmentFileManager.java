package Fall2026.infrastructure.persistence;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentFileManager {

    private static final String APPOINTMENTS_FILE = "Appointments.txt";

    private final CredentialStorage storage;
    private final Schedule schedule;

    public AppointmentFileManager(Schedule schedule) {
        this.storage  = new CredentialStorage();
        this.schedule = schedule;
    }


    // Package-private: used only by tests
    AppointmentFileManager(Schedule schedule, CredentialStorage storage) {
        this.schedule = schedule;
        this.storage  = storage;
    }

    /**
     * Save all appointments to Appointments.txt
     * Format: date,startTime,endTime,type,status,description,currentBookings
     * Example: 2026-04-15,09:00,10:00,URGENT,CONFIRMED,checkup,1
     */
    public void saveAppointmentsToFile(List<Appointment> appointments) {
        List<String> lines = new ArrayList<>();
        for (Appointment appt : appointments) {
            TimeSlot slot = appt.getTimeSlot();
            String line = slot.getDate()        + ","
                    + slot.getStartTime()        + ","
                    + slot.getEndTime()          + ","
                    + appt.getAppointmentType()  + ","
                    + appt.getStatus()           + ","
                    + appt.getDescription()      + ","
                    + appt.getCurrentBookings();
            lines.add(line);
        }

        if (!storage.WriteToFile(APPOINTMENTS_FILE, lines)) {
            System.out.println("Failed to save appointments to file.");
        }
    }

    /**
     * Load appointments from Appointments.txt on startup.
     * Matches each appointment back to its TimeSlot in the schedule.
     * Returns a list of loaded appointments.
     */
    public List<Appointment> loadAppointmentsFromFile() {
        List<Appointment> appointments = new ArrayList<>();
        List<String> lines = storage.ReadFromFile(APPOINTMENTS_FILE);

        if (lines.isEmpty()) {
            return appointments;
        }

        for (String line : lines) {
            try {
                String[] parts = line.split(",", 7);
                if (parts.length != 7) {
                    System.out.println("Skipping invalid appointment line: " + line);
                    continue;
                }

                LocalDate date         = LocalDate.parse(parts[0].trim());
                LocalTime startTime    = LocalTime.parse(parts[1].trim());
                LocalTime endTime      = LocalTime.parse(parts[2].trim());
                AppointmentType type   = AppointmentType.valueOf(parts[3].trim());
                Appointment.AppointmentStatus status =
                        Appointment.AppointmentStatus.valueOf(parts[4].trim());
                String description     = parts[5].trim();
                int currentBookings    = Integer.parseInt(parts[6].trim());

                // find the matching TimeSlot in the schedule
                TimeSlot matchedSlot = findSlot(date, startTime, endTime);
                if (matchedSlot == null) {
                    System.out.println("No matching slot found for appointment: " + line);
                    continue;
                }

                Appointment appt = new Appointment(
                        matchedSlot, description, matchedSlot.getMaxCapacity(), type);
                appt.setStatus(status);

                // restore booking count
                for (int i = 0; i < currentBookings; i++) {
                    appt.addBooking();
                }

                appointments.add(appt);

            } catch (Exception e) {
                System.out.println("Error parsing appointment line '" + line + "': " + e.getMessage());
            }
        }

        System.out.println("Loaded " + appointments.size() + " appointments from file.");
        return appointments;
    }

    private TimeSlot findSlot(LocalDate date, LocalTime start, LocalTime end) {
        for (TimeSlot slot : schedule.getAllSlots()) {
            if (slot.getDate().equals(date)
                    && slot.getStartTime().equals(start)
                    && slot.getEndTime().equals(end)) {
                return slot;
            }
        }
        return null;
    }
}