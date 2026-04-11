package Fall2026.domain.appointment;

import Fall2026.domain.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeSlotTest {

    private LocalDate date;
    private LocalTime start;
    private LocalTime end;

    @BeforeEach
    void setUp() {
        date = LocalDate.of(2026, 11, 20);
        start = LocalTime.of(10, 0);
        end = LocalTime.of(11, 0);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("creates time slot with valid values")
        void createsWithValidValues() {
            TimeSlot slot = new TimeSlot(date, start, end, 3);
            assertEquals(date, slot.getDate());
            assertEquals(start, slot.getStartTime());
            assertEquals(end, slot.getEndTime());
            assertEquals(3, slot.getMaxCapacity());
        }

        @Test
        @DisplayName("throws when end time is before start time")
        void throwsWhenEndBeforeStart() {
            assertThrows(ValidationException.class,
                    () -> new TimeSlot(date, LocalTime.of(12, 0), LocalTime.of(11, 0), 2));
        }

        @Test
        @DisplayName("throws when end time equals start time")
        void throwsWhenEndEqualsStart() {
            assertThrows(ValidationException.class,
                    () -> new TimeSlot(date, LocalTime.NOON, LocalTime.NOON, 2));
        }

        @ParameterizedTest(name = "rejects capacity {0}")
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("throws when capacity is below one")
        void throwsWhenCapacityBelowOne(int invalidCapacity) {
            assertThrows(ValidationException.class,
                    () -> new TimeSlot(date, start, end, invalidCapacity));
        }

        @Test
        @DisplayName("accepts boundary capacity value of one")
        void acceptsBoundaryCapacityOne() {
            TimeSlot slot = new TimeSlot(date, start, end, 1);
            assertEquals(1, slot.getMaxCapacity());
        }
    }

    @Nested
    @DisplayName("getDuration")
    class GetDurationTests {

        @Test
        @DisplayName("returns correct duration for one-hour slot")
        void returnsCorrectDurationOneHour() {
            TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
            assertEquals(Duration.ofHours(1), slot.getDuration());
        }

        @Test
        @DisplayName("returns correct duration for non-round interval")
        void returnsCorrectDurationNonRoundInterval() {
            TimeSlot slot = new TimeSlot(date, LocalTime.of(9, 15), LocalTime.of(10, 45), 1);
            assertEquals(Duration.ofMinutes(90), slot.getDuration());
        }

        @Test
        @DisplayName("returns boundary duration of one minute")
        void returnsBoundaryOneMinute() {
            TimeSlot slot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(10, 1), 1);
            assertEquals(Duration.ofMinutes(1), slot.getDuration());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualityTests {

        @Test
        @DisplayName("equal objects have same hash code")
        void equalObjectsHaveSameHashCode() {
            TimeSlot first = new TimeSlot(date, start, end, 1);
            TimeSlot second = new TimeSlot(date, start, end, 9);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        @DisplayName("different date or times produce inequality")
        void differentDateOrTimesAreNotEqual() {
            TimeSlot original = new TimeSlot(date, start, end, 1);
            TimeSlot differentDate = new TimeSlot(date.plusDays(1), start, end, 1);
            TimeSlot differentStart = new TimeSlot(date, start.plusMinutes(15), end.plusMinutes(15), 1);

            assertNotEquals(original, differentDate);
            assertNotEquals(original, differentStart);
        }

        @Test
        @DisplayName("hash code is consistent across repeated calls")
        void hashCodeIsConsistent() {
            TimeSlot slot = new TimeSlot(date, start, end, 2);
            int first = slot.hashCode();
            int second = slot.hashCode();
            assertEquals(first, second);
        }
    }
}
