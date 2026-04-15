package Fall2026.application.rules;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GroupRuleStrategy")
class GroupRuleStrategyTest {

    private GroupRuleStrategy strategy;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        strategy = new GroupRuleStrategy();
        testDate = LocalDate.now().plusDays(1);
    }

    @Nested
    @DisplayName("getMaxDurationMinutes")
    class GetMaxDurationMinutesTests {

        @Test
        @DisplayName("returns exactly 120 minutes")
        void returnsExactly120Minutes() {
            assertEquals(120, strategy.getMaxDurationMinutes());
        }
    }

    @Nested
    @DisplayName("getMaxCapacity")
    class GetMaxCapacityTests {

        @Test
        @DisplayName("returns exactly 20")
        void returnsExactly20() {
            assertEquals(20, strategy.getMaxCapacity());
        }
    }

    @Nested
    @DisplayName("isAutoApproved")
    class IsAutoApprovedTests {

        @Test
        @DisplayName("returns false")
        void returnsFalse() {
            assertFalse(strategy.isAutoApproved());
        }
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("passes for appointment at max duration (120 minutes)")
        void passesAtMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 0), 20);
            Appointment appointment = new Appointment(slot, "desc", 20, AppointmentType.GROUP, "");

            strategy.validate(appointment);
        }

        @Test
        @DisplayName("throws for appointment one minute over max duration")
        void throwsWhenOverMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 1), 20);
            Appointment appointment = new Appointment(slot, "desc", 20, AppointmentType.GROUP, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws for appointment with capacity exceeding max")
        void throwsWhenCapacityExceedsMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 0), 21);
            Appointment appointment = new Appointment(slot, "desc", 21, AppointmentType.GROUP, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }
    }
}
