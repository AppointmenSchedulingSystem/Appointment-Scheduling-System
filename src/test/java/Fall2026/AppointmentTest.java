package Fall2026;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private List<Appointment> allSlots;
    private Appointment availableSlot;
    private Appointment fullyBookedSlot;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2026, 1, 15, 10, 0);
        allSlots = new ArrayList<>();

        // Create an available slot with capacity 2
        availableSlot = new Appointment(testDateTime, "Consultation", 2);

        // Create a fully booked slot
        fullyBookedSlot = new Appointment(LocalDateTime.of(2026, 1, 15, 11, 0), "Meeting", 1);
        fullyBookedSlot.bookAppointment();

        allSlots.add(availableSlot);
        allSlots.add(fullyBookedSlot);
    }

    @AfterEach
    void tearDown() {
        allSlots = null;
        availableSlot = null;
        fullyBookedSlot = null;
    }

    @Test
    void getAvailableSlots() {
        List<Appointment> available = Appointment.getAvailableSlots(allSlots);

        assertEquals(1, available.size(), "Should return only 1 available slot");
        assertTrue(available.contains(availableSlot), "Available slot should be in results");
        assertFalse(available.contains(fullyBookedSlot), "Fully booked slot should not be in results");
    }

    @Test
    void bookAppointment() {
        assertTrue(availableSlot.bookAppointment(), "Booking should succeed when slots available");
        assertEquals(1, availableSlot.getCurrentBookings(), "Current bookings should increment");
        assertTrue(availableSlot.isAvailable(), "Slot should still be available");

        assertTrue(availableSlot.bookAppointment(), "Second booking should succeed");
        assertEquals(2, availableSlot.getCurrentBookings(), "Current bookings should be 2");
        assertFalse(availableSlot.isAvailable(), "Slot should not be available when full");
    }

    @Test
    void bookAppointmentWhenFull() {
        assertFalse(fullyBookedSlot.bookAppointment(), "Booking should fail when slot is full");
        assertEquals(1, fullyBookedSlot.getCurrentBookings(), "Bookings should not increase");
    }

    @Test
    void getDateTime() {
        assertEquals(testDateTime, availableSlot.getDateTime(), "DateTime should match");
    }

    @Test
    void getDescription() {
        assertEquals("Consultation", availableSlot.getDescription(), "Description should match");
    }

    @Test
    void getMaxCapacity() {
        assertEquals(2, availableSlot.getMaxCapacity(), "Max capacity should be 2");
    }

    @Test
    void getCurrentBookings() {
        assertEquals(0, availableSlot.getCurrentBookings(), "Initial bookings should be 0");
    }

    @Test
    void isAvailable() {
        assertTrue(availableSlot.isAvailable(), "New slot should be available");
        assertFalse(fullyBookedSlot.isAvailable(), "Fully booked slot should not be available");
    }
}