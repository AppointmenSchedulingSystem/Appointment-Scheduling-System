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

@DisplayName("AssessmentRuleStrategy")
class AssessmentRuleStrategyTest {

    private AssessmentRuleStrategy strategy;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        strategy = new AssessmentRuleStrategy();
        testDate = LocalDate.now().plusDays(1);
    }

    @Nested
    @DisplayName("getMaxDurationMinutes")
    class GetMaxDurationMinutesTests {

        @Test
        @DisplayName("returns exactly 90 minutes")
        void returnsExactly90Minutes() {
            assertEquals(90, strategy.getMaxDurationMinutes());
        }
    }

    @Nested
    @DisplayName("getMaxCapacity")
    class GetMaxCapacityTests {

        @Test
        @DisplayName("returns exactly 2")
        void returnsExactly2() {
            assertEquals(2, strategy.getMaxCapacity());
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
        @DisplayName("passes for appointment at max duration (90 minutes)")
        void passesAtMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 30), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.ASSESSMENT, "");

            strategy.validate(appointment);
        }

        @Test
        @DisplayName("throws for appointment one minute over max duration")
        void throwsWhenOverMaxDuration() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 31), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.ASSESSMENT, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws for appointment with capacity exceeding max")
        void throwsWhenCapacityExceedsMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 30), 3);
            Appointment appointment = new Appointment(slot, "desc", 3, AppointmentType.ASSESSMENT, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }
    }
}
