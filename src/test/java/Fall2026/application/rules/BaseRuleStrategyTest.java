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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BaseRuleStrategy")
class BaseRuleStrategyTest {

    private BookingRuleStrategy strategy;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        strategy = new UrgentRuleStrategy();
        testDate = LocalDate.now().plusDays(1);
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("passes when duration is exactly at max limit (boundary)")
        void passesWhenDurationAtMaxBoundary() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 30), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.URGENT, "");

            assertDoesNotThrow(() -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws when duration exceeds max by one minute")
        void throwsWhenDurationExceedsByOneMinute() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 31), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.URGENT, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("passes when capacity is exactly at max limit (boundary)")
        void passesWhenCapacityAtMaxBoundary() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 30), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.URGENT, "");

            assertDoesNotThrow(() -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws when capacity exceeds max limit")
        void throwsWhenCapacityExceedsMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 30), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.URGENT, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("throws when both duration and capacity exceed max limits")
        void throwsWhenBothExceedMax() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.URGENT, "");

            assertThrows(ValidationException.class, () -> strategy.validate(appointment));
        }

        @Test
        @DisplayName("passes when appointment is well within limits")
        void passesWhenWellWithinLimits() {
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(9, 15), 1);
            Appointment appointment = new Appointment(slot, "desc", 1, AppointmentType.URGENT, "");

            assertDoesNotThrow(() -> strategy.validate(appointment));
        }
    }

    @Nested
    @DisplayName("validate with different strategies")
    class ValidateWithDifferentStrategiesTests {

        @Test
        @DisplayName("AssessmentRuleStrategy passes at its max duration (90 minutes)")
        void assessmentPassesAtMax() {
            BookingRuleStrategy assessmentStrategy = new AssessmentRuleStrategy();
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 30), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.ASSESSMENT, "");

            assertDoesNotThrow(() -> assessmentStrategy.validate(appointment));
        }

        @Test
        @DisplayName("AssessmentRuleStrategy throws when exceeding 90 minutes")
        void assessmentThrowsOver90Minutes() {
            BookingRuleStrategy assessmentStrategy = new AssessmentRuleStrategy();
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(10, 31), 2);
            Appointment appointment = new Appointment(slot, "desc", 2, AppointmentType.ASSESSMENT, "");

            assertThrows(ValidationException.class, () -> assessmentStrategy.validate(appointment));
        }

        @Test
        @DisplayName("GroupRuleStrategy passes at max capacity (20)")
        void groupPassesAtMaxCapacity() {
            BookingRuleStrategy groupStrategy = new GroupRuleStrategy();
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 0), 20);
            Appointment appointment = new Appointment(slot, "desc", 20, AppointmentType.GROUP, "");

            assertDoesNotThrow(() -> groupStrategy.validate(appointment));
        }

        @Test
        @DisplayName("GroupRuleStrategy throws when capacity exceeds 20")
        void groupThrowsOver20Capacity() {
            BookingRuleStrategy groupStrategy = new GroupRuleStrategy();
            TimeSlot slot = new TimeSlot(testDate, LocalTime.of(9, 0), LocalTime.of(11, 0), 21);
            Appointment appointment = new Appointment(slot, "desc", 21, AppointmentType.GROUP, "");

            assertThrows(ValidationException.class, () -> groupStrategy.validate(appointment));
        }
    }
}
