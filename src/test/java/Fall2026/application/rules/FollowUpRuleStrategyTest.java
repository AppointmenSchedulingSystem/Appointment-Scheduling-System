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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FollowUpRuleStrategy")
class FollowUpRuleStrategyTest {

    private FollowUpRuleStrategy strategy;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        strategy = new FollowUpRuleStrategy();
        testDate = LocalDate.now().plusDays(1);
    }

    @Nested
    @DisplayName("getMaxDurationMinutes")
    class GetMaxDurationMinutesTests {

        @Test
        @DisplayName("returns exactly 45 minutes")
        void returnsExactly45Minutes() {
            assertEquals(45, strategy.getMaxDurationMinutes());
        }
    }

    @Nested
    @DisplayName("getMaxCapacity")
    class GetMaxCapacityTests {

        @Test
        @DisplayName("returns exactly 1")
        void returnsExactly1() {
            assertEquals(1, strategy.getMaxCapacity());
        }
    }

    @Nested
    @DisplayName("isAutoApproved")
    class IsAutoApprovedTests {

        @Test
        @DisplayName("returns true")
        void returnsTrue() {
            assertTrue(strategy.isAutoApproved());
        }
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("passes for appointment at max duration (45 minutes)")
        void passesAtMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 45), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.FOLLOW_UP);

            strategy.validate(appointment);
        }

        @Test
        @DisplayName("throws for appointment one minute over max duration")
        void throwsWhenOverMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 46), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.FOLLOW_UP);

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws for appointment with capacity exceeding max")
        void throwsWhenCapacityExceedsMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 45), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.FOLLOW_UP);

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }
    }
}
