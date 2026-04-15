package Fall2026.domain.appointment;

import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentTest {

    private TimeSlot validSlot;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        validSlot = new TimeSlot(LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
        appointment = new Appointment(validSlot, "Consultation", 2, AppointmentType.IN_PERSON, "");
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("creates appointment with valid inputs")
        void createsWithValidInputs() {
            Appointment created = new Appointment(validSlot, "Check-up", 3, AppointmentType.ASSESSMENT, "");
            assertEquals(validSlot, created.getTimeSlot());
            assertEquals("Check-up", created.getDescription());
            assertEquals(3, created.getMaxCapacity());
            assertEquals(0, created.getCurrentBookings());
            assertEquals(Appointment.AppointmentStatus.CONFIRMED, created.getStatus());
        }

        @Test
        @DisplayName("throws when time slot is null")
        void throwsWhenTimeSlotIsNull() {
            assertThrows(ValidationException.class,
                    () -> new Appointment(null, "desc", 1, AppointmentType.GROUP, ""));
        }

        @Test
        @DisplayName("throws when appointment type is null")
        void throwsWhenAppointmentTypeIsNull() {
            assertThrows(ValidationException.class,
                    () -> new Appointment(validSlot, "desc", 1, null, ""));
        }

        @Test
        @DisplayName("throws when capacity is zero")
        void throwsWhenCapacityIsZero() {
            assertThrows(ValidationException.class,
                    () -> new Appointment(validSlot, "desc", 0, AppointmentType.INDIVIDUAL, ""));
        }

        @Test
        @DisplayName("throws when capacity is negative")
        void throwsWhenCapacityIsNegative() {
            assertThrows(ValidationException.class,
                    () -> new Appointment(validSlot, "desc", -1, AppointmentType.INDIVIDUAL, ""));
        }

        @Test
        @DisplayName("uses empty description when null is provided")
        void usesEmptyDescriptionForNullInput() {
            Appointment created = new Appointment(validSlot, null, 1, AppointmentType.VIRTUAL, "");
            assertEquals("", created.getDescription());
        }
    }

    @Nested
    @DisplayName("addBooking")
    class AddBookingTests {

        @Test
        @DisplayName("adds booking and returns true when below capacity")
        void addsBookingBelowCapacity() {
            boolean added = appointment.addBooking();

            assertTrue(added);
            assertEquals(1, appointment.getCurrentBookings());
        }

        @Test
        @DisplayName("returns false and does not increment when at capacity")
        void returnsFalseAtCapacity() {
            appointment.addBooking();
            appointment.addBooking();

            boolean added = appointment.addBooking();

            assertFalse(added);
            assertEquals(2, appointment.getCurrentBookings());
        }

        @Test
        @DisplayName("handles boundary capacity of one")
        void handlesBoundaryCapacityOne() {
            Appointment single = new Appointment(validSlot, "Single", 1, AppointmentType.INDIVIDUAL, "");

            assertTrue(single.addBooking());
            assertFalse(single.addBooking());
            assertEquals(1, single.getCurrentBookings());
        }
    }

    @Nested
    @DisplayName("removeBooking")
    class RemoveBookingTests {

        @Test
        @DisplayName("decrements booking count when bookings exist")
        void decrementsBookingCountWhenBookingsExist() {
            appointment.addBooking();
            appointment.addBooking();

            appointment.removeBooking();

            assertEquals(1, appointment.getCurrentBookings());
        }

        @Test
        @DisplayName("throws when trying to remove booking from empty appointment")
        void throwsWhenRemovingFromEmpty() {
            assertThrows(ValidationException.class, () -> appointment.removeBooking());
        }

        @Test
        @DisplayName("handles boundary transition from one booking to zero")
        void handlesBoundaryOneToZero() {
            appointment.addBooking();

            appointment.removeBooking();

            assertEquals(0, appointment.getCurrentBookings());
        }
    }

    @Nested
    @DisplayName("isFull")
    class IsFullTests {

        @Test
        @DisplayName("returns false when bookings are below capacity")
        void returnsFalseBelowCapacity() {
            appointment.addBooking();
            assertFalse(appointment.isFull());
        }

        @Test
        @DisplayName("returns true at exact capacity")
        void returnsTrueAtExactCapacity() {
            appointment.addBooking();
            appointment.addBooking();
            assertTrue(appointment.isFull());
        }

        @Test
        @DisplayName("handles boundary of zero bookings")
        void handlesBoundaryZeroBookings() {
            assertFalse(appointment.isFull());
        }
    }

    @Nested
    @DisplayName("setStatus and getStatus")
    class StatusTests {

        @ParameterizedTest(name = "stores and returns status: {0}")
        @EnumSource(Appointment.AppointmentStatus.class)
        @DisplayName("stores and returns each status enum value")
        void storesAndReturnsAllStatusValues(Appointment.AppointmentStatus status) {
            appointment.setStatus(status);
            assertEquals(status, appointment.getStatus());
        }

        @Test
        @DisplayName("accepts null as a boundary status value")
        void acceptsNullStatusBoundary() {
            appointment.setStatus(null);
            assertEquals(null, appointment.getStatus());
        }
    }

    @Nested
    @DisplayName("getAppointmentType")
    class GetAppointmentTypeTests {

        @Test
        @DisplayName("returns the appointment type provided at construction")
        void returnsConstructorProvidedType() {
            assertEquals(AppointmentType.IN_PERSON, appointment.getAppointmentType());
        }

        @Test
        @DisplayName("returns correct value for another enum option")
        void returnsAnotherEnumOption() {
            Appointment virtual = new Appointment(validSlot, "Virtual", 1, AppointmentType.VIRTUAL, "");
            assertEquals(AppointmentType.VIRTUAL, virtual.getAppointmentType());
        }

        @Test
        @DisplayName("handles boundary enum value at declaration end")
        void handlesBoundaryEnumValueEnd() {
            Appointment group = new Appointment(validSlot, "Group", 4, AppointmentType.GROUP, "");
            assertEquals(AppointmentType.GROUP, group.getAppointmentType());
        }
    }
}
