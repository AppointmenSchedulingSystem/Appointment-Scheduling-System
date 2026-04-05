package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestBooking {

    public static void main(String[] args) {

        // 1️⃣ Create mock notification service
        MockNotificationService mock = new MockNotificationService();

        // 2️⃣ Create AppointmentService with mock
        AppointmentService service = new AppointmentService(mock);

        // 3️⃣ Create a time slot
        TimeSlot slot = new TimeSlot(
                LocalDate.of(2026, 4, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // 4️⃣ Create an appointment
        Appointment appt = new Appointment(slot, "Checkup", 5);
        appt.setUserEmail("test@example.com");

        // 5️⃣ Book the appointment
        service.bookAppointment(appt);

        // 6️⃣ Check messages sent by the mock
        System.out.println("Sent notifications:");
        for (String msg : mock.getSentMessages()) {
            System.out.println(msg);
        }
    }
}