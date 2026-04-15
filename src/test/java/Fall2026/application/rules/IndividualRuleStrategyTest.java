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

@DisplayName("IndividualRuleStrategy")
class IndividualRuleStrategyTest {

    private IndividualRuleStrategy strategy;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        strategy = new IndividualRuleStrategy();
        testDate = LocalDate.now().plusDays(1);
    }

    @Nested
    @DisplayName("getMaxDurationMinutes")
    class GetMaxDurationMinutesTests {

        @Test
        @DisplayName("returns exactly 60 minutes")
        void returnsExactly60Minutes() {
            assertEquals(60, strategy.getMaxDurationMinutes());
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
        @DisplayName("passes for appointment at max duration (60 minutes)")
        void passesAtMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.INDIVIDUAL, "");

            strategy.validate(appointment);
        }

        @Test
        @DisplayName("throws for appointment one minute over max duration")
        void throwsWhenOverMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 1), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.INDIVIDUAL, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws for appointment with capacity exceeding max")
        void throwsWhenCapacityExceedsMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.INDIVIDUAL, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }
    }
}
