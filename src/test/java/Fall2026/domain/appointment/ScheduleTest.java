package Fall2026.domain.appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleTest {

    private Schedule schedule;
    private TimeSlot slotDayOneMorning;
    private TimeSlot slotDayOneAfternoon;
    private TimeSlot slotDayTwoMorning;

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        slotDayOneMorning = new TimeSlot(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
        slotDayOneAfternoon = new TimeSlot(LocalDate.of(2026, 10, 1), LocalTime.of(14, 0), LocalTime.of(15, 0), 1);
        slotDayTwoMorning = new TimeSlot(LocalDate.of(2026, 10, 2), LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
    }

    @Nested
    @DisplayName("addSlot")
    class AddSlotTests {

        @Test
        @DisplayName("adds a slot to the schedule")
        void addsSlot() {
            schedule.addSlot(slotDayOneMorning);
            assertEquals(1, schedule.getAllSlots().size());
            assertTrue(schedule.getAllSlots().contains(slotDayOneMorning));
        }

        @Test
        @DisplayName("allows duplicate slots as an error-tolerant behavior")
        void allowsDuplicateSlots() {
            schedule.addSlot(slotDayOneMorning);
            schedule.addSlot(slotDayOneMorning);
            assertEquals(2, schedule.getAllSlots().size());
        }

        @Test
        @DisplayName("handles boundary by adding first slot to empty schedule")
        void addsFirstSlotToEmptySchedule() {
            assertTrue(schedule.getAllSlots().isEmpty());
            schedule.addSlot(slotDayOneAfternoon);
            assertEquals(1, schedule.getAllSlots().size());
        }
    }

    @Nested
    @DisplayName("removeSlot")
    class RemoveSlotTests {

        @Test
        @DisplayName("removes existing slot")
        void removesExistingSlot() {
            schedule.addSlot(slotDayOneMorning);
            schedule.removeSlot(slotDayOneMorning);
            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("keeps schedule unchanged when removing non-existent slot")
        void removingNonExistentSlotKeepsState() {
            schedule.addSlot(slotDayOneMorning);
            schedule.removeSlot(slotDayTwoMorning);
            assertEquals(1, schedule.getAllSlots().size());
            assertTrue(schedule.getAllSlots().contains(slotDayOneMorning));
        }

        @Test
        @DisplayName("handles boundary removal from empty schedule")
        void removesFromEmptyScheduleWithoutFailure() {
            schedule.removeSlot(slotDayOneMorning);
            assertTrue(schedule.getAllSlots().isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllSlots")
    class GetAllSlotsTests {

        @Test
        @DisplayName("returns all added slots")
        void returnsAllAddedSlots() {
            schedule.addSlot(slotDayOneMorning);
            schedule.addSlot(slotDayTwoMorning);

            List<TimeSlot> all = schedule.getAllSlots();

            assertEquals(2, all.size());
            assertTrue(all.contains(slotDayOneMorning));
            assertTrue(all.contains(slotDayTwoMorning));
        }

        @Test
        @DisplayName("returns empty list when schedule has no slots")
        void returnsEmptyListWhenNoSlots() {
            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("returns defensive copy at boundary of external modification")
        void returnsDefensiveCopy() {
            schedule.addSlot(slotDayOneMorning);
            List<TimeSlot> copy = schedule.getAllSlots();
            copy.clear();
            assertEquals(1, schedule.getAllSlots().size());
        }
    }

    @Nested
    @DisplayName("getAvailableSlotsForDay")
    class GetAvailableSlotsForDayTests {

        @Test
        @DisplayName("returns only slots for requested date")
        void returnsOnlyRequestedDateSlots() {
            schedule.addSlot(slotDayOneMorning);
            schedule.addSlot(slotDayOneAfternoon);
            schedule.addSlot(slotDayTwoMorning);

            List<TimeSlot> dayOneSlots = schedule.getAvailableSlotsForDay(LocalDate.of(2026, 10, 1));

            assertEquals(2, dayOneSlots.size());
            assertTrue(dayOneSlots.contains(slotDayOneMorning));
            assertTrue(dayOneSlots.contains(slotDayOneAfternoon));
            assertFalse(dayOneSlots.contains(slotDayTwoMorning));
        }

        @Test
        @DisplayName("returns empty list when no slots exist for requested date")
        void returnsEmptyWhenNoDateMatches() {
            schedule.addSlot(slotDayOneMorning);
            List<TimeSlot> slots = schedule.getAvailableSlotsForDay(LocalDate.of(2026, 10, 3));
            assertTrue(slots.isEmpty());
        }

        @Test
        @DisplayName("handles boundary date with exactly one slot")
        void handlesBoundaryDateWithOneSlot() {
            schedule.addSlot(slotDayTwoMorning);
            List<TimeSlot> slots = schedule.getAvailableSlotsForDay(LocalDate.of(2026, 10, 2));
            assertEquals(1, slots.size());
            assertEquals(slotDayTwoMorning, slots.getFirst());
        }
    }

    @Nested
    @DisplayName("getAvailableDays")
    class GetAvailableDaysTests {

        @Test
        @DisplayName("returns distinct dates from available slots")
        void returnsDistinctDates() {
            schedule.addSlot(slotDayOneMorning);
            schedule.addSlot(slotDayOneAfternoon);
            schedule.addSlot(slotDayTwoMorning);

            List<LocalDate> days = schedule.getAvailableDays();

            assertEquals(2, days.size());
            assertEquals(LocalDate.of(2026, 10, 1), days.get(0));
            assertEquals(LocalDate.of(2026, 10, 2), days.get(1));
        }

        @Test
        @DisplayName("returns empty list when schedule has no slots")
        void returnsEmptyWhenNoSlots() {
            assertTrue(schedule.getAvailableDays().isEmpty());
        }

        @Test
        @DisplayName("handles boundary case with repeated same-day slots")
        void handlesBoundaryWithRepeatedSameDaySlots() {
            schedule.addSlot(slotDayOneMorning);
            schedule.addSlot(slotDayOneAfternoon);

            List<LocalDate> days = schedule.getAvailableDays();

            assertEquals(1, days.size());
            assertEquals(LocalDate.of(2026, 10, 1), days.getFirst());
        }
    }
}
