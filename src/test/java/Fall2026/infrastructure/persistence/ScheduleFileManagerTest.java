package Fall2026.infrastructure.persistence;

import Fall2026.domain.appointment.Schedule;
import Fall2026.domain.appointment.TimeSlot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ScheduleFileManager.
 *
 * Mock strategy:
 * - CredentialStorage: always mocked — it performs real file I/O.
 * - Schedule: pure in-memory domain class, always instantiated directly.
 * - TimeSlot: pure domain class, always instantiated directly.
 *
 * ⚠ Requires one source change to be testable:
 *   Add a package-private constructor to ScheduleFileManager that accepts
 *   both dependencies so the mocks can be injected:
 *
 *     ScheduleFileManager(Schedule schedule, CredentialStorage storage) {
 *         this.schedule = schedule;
 *         this.storage  = storage;
 *         loadSlotsFromFile();
 *     }
 *
 *   The two existing public constructors are unchanged for production use.
 */
@DisplayName("ScheduleFileManager Tests")
class ScheduleFileManagerTest {

    private static final String SLOTS_FILE = "Slots.txt";

    // ─── Mocked dependency ───────────────────────────────────────────────────

    @Mock
    private CredentialStorage storage;

    // ─── Real collaborators ──────────────────────────────────────────────────

    private Schedule schedule;
    private ScheduleFileManager manager;

    // ─── Shared fixtures ─────────────────────────────────────────────────────

    private TimeSlot slotA;   // 2026-12-01  09:00–10:00  cap 1
    private TimeSlot slotB;   // 2026-12-01  11:00–12:00  cap 2
    private TimeSlot slotC;   // 2026-12-02  09:00–10:00  cap 3

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        slotA = new TimeSlot(LocalDate.of(2026, 12, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
        slotB = new TimeSlot(LocalDate.of(2026, 12, 1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), 2);
        slotC = new TimeSlot(LocalDate.of(2026, 12, 2),
                LocalTime.of(9, 0), LocalTime.of(10, 0), 3);

        schedule = new Schedule();

        // Default: file is empty so loadSlotsFromFile() is a no-op at construction
        when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(new ArrayList<>());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        reset(storage);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Builds the 4-field CSV line exactly as ScheduleFileManager writes it. */
    private String slotLine(TimeSlot slot) {
        return slot.getDate() + ","
                + slot.getStartTime() + ","
                + slot.getEndTime()   + ","
                + slot.getMaxCapacity();
    }

    /** Creates a manager with the mock storage and a fresh schedule. */
    private ScheduleFileManager buildManager() {
        return new ScheduleFileManager(schedule, storage);
    }

    // =========================================================================
    // Constructor — loadSlotsFromFile behaviour
    // =========================================================================

    @Nested
    @DisplayName("Constructor — loadSlotsFromFile")
    class ConstructorLoadTests {

        @Test
        @DisplayName("schedule remains empty when file has no lines")
        void scheduleEmptyWhenFileEmpty() {
            when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(new ArrayList<>());

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("loads a single valid slot line into the schedule")
        void loadsSingleValidSlot() {
            when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(List.of(slotLine(slotA)));

            buildManager();

            List<TimeSlot> slots = schedule.getAllSlots();
            assertEquals(1, slots.size());
            assertEquals(slotA, slots.get(0));
        }

        @Test
        @DisplayName("loads multiple valid slot lines into the schedule")
        void loadsMultipleValidSlots() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of(slotLine(slotA), slotLine(slotB), slotLine(slotC)));

            buildManager();

            assertEquals(3, schedule.getAllSlots().size());
        }

        @Test
        @DisplayName("restores date, start time, end time, and capacity correctly")
        void restoresAllFieldsCorrectly() {
            when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(List.of(slotLine(slotB)));

            buildManager();

            TimeSlot loaded = schedule.getAllSlots().get(0);
            assertEquals(LocalDate.of(2026, 12, 1),  loaded.getDate());
            assertEquals(LocalTime.of(11, 0),         loaded.getStartTime());
            assertEquals(LocalTime.of(12, 0),         loaded.getEndTime());
            assertEquals(2,                            loaded.getMaxCapacity());
        }

        @Test
        @DisplayName("skips a line that has fewer than 4 comma-separated parts")
        void skipsLineTooFewParts() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of("2026-12-01,09:00,10:00")); // 3 parts — missing capacity

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("skips a line that has more than 4 comma-separated parts")
        void skipsLineTooManyParts() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of("2026-12-01,09:00,10:00,1,extra"));

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("skips a line with an unparseable date")
        void skipsLineWithBadDate() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of("not-a-date,09:00,10:00,1"));

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("skips a line with an unparseable time")
        void skipsLineWithBadTime() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of("2026-12-01,bad-time,10:00,1"));

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("skips a line with a non-integer capacity")
        void skipsLineWithNonIntegerCapacity() {
            when(storage.ReadFromFile(SLOTS_FILE))
                    .thenReturn(List.of("2026-12-01,09:00,10:00,abc"));

            buildManager();

            assertTrue(schedule.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("loads valid lines and skips invalid ones in the same file")
        void loadsValidAndSkipsInvalid() {
            when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(List.of(
                    slotLine(slotA),            // valid
                    "garbage,data",             // invalid — 2 parts
                    slotLine(slotC)             // valid
            ));

            buildManager();

            List<TimeSlot> slots = schedule.getAllSlots();
            assertEquals(2, slots.size());
            assertTrue(slots.contains(slotA));
            assertTrue(slots.contains(slotC));
        }

        @Test
        @DisplayName("loads into an existing schedule that already contains slots")
        void loadsIntoPrePopulatedSchedule() {
            schedule.addSlot(slotA);
            when(storage.ReadFromFile(SLOTS_FILE)).thenReturn(List.of(slotLine(slotB)));

            buildManager();

            // slotA was already there; slotB added by load — both present
            assertEquals(2, schedule.getAllSlots().size());
        }
    }

    // =========================================================================
    // saveSlotsToFile
    // =========================================================================

    @Nested
    @DisplayName("saveSlotsToFile")
    class SaveSlotsToFileTests {

        @Test
        @DisplayName("writes a single slot as a correctly formatted CSV line")
        void writesSingleSlotCorrectly() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            mgr.saveSlotsToFile();

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(SLOTS_FILE), captor.capture());

            List<String> written = captor.getValue();
            assertEquals(1, written.size());
            assertEquals(slotLine(slotA), written.get(0));
        }

        @Test
        @DisplayName("writes multiple slots as separate lines in schedule order")
        void writesMultipleSlotsInOrder() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            schedule.addSlot(slotA);
            schedule.addSlot(slotB);
            schedule.addSlot(slotC);
            ScheduleFileManager mgr = buildManager();

            mgr.saveSlotsToFile();

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(SLOTS_FILE), captor.capture());

            List<String> written = captor.getValue();
            assertEquals(3, written.size());
            assertEquals(slotLine(slotA), written.get(0));
            assertEquals(slotLine(slotB), written.get(1));
            assertEquals(slotLine(slotC), written.get(2));
        }

        @Test
        @DisplayName("writes an empty list when schedule has no slots")
        void writesEmptyListWhenScheduleEmpty() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.saveSlotsToFile();

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(SLOTS_FILE), captor.capture());

            assertTrue(captor.getValue().isEmpty());
        }

        @Test
        @DisplayName("delegates to storage even when WriteToFile returns false")
        void delegatesToStorageOnWriteFailure() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(false);
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            // Must not throw — failure is logged, not propagated
            mgr.saveSlotsToFile();

            verify(storage).WriteToFile(eq(SLOTS_FILE), any());
        }
    }

    // =========================================================================
    // addTimeSlot (date/time/capacity overload)
    // =========================================================================

    @Nested
    @DisplayName("addTimeSlot — date/time/capacity parameters")
    class AddTimeSlotParamsTests {

        @Test
        @DisplayName("adds slot to the schedule")
        void addsSlotToSchedule() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(
                    LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    1
            );

            assertEquals(1, schedule.getAllSlots().size());
        }

        @Test
        @DisplayName("saves to file after adding the slot")
        void savesToFileAfterAdding() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(
                    LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    1
            );

            verify(storage).WriteToFile(eq(SLOTS_FILE), any());
        }

        @Test
        @DisplayName("boundary: adding a slot with capacity of one succeeds")
        void addsBoundaryCapacityOne() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(
                    LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    1
            );

            assertEquals(1, schedule.getAllSlots().get(0).getMaxCapacity());
        }
    }

    // =========================================================================
    // addTimeSlot (TimeSlot object overload)
    // =========================================================================

    @Nested
    @DisplayName("addTimeSlot — TimeSlot object parameter")
    class AddTimeSlotObjectTests {

        @Test
        @DisplayName("adds the slot object to the schedule")
        void addsSlotObjectToSchedule() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(slotA);

            assertTrue(schedule.getAllSlots().contains(slotA));
        }

        @Test
        @DisplayName("saves to file after adding the slot object")
        void savesToFileAfterAddingObject() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(slotA);

            verify(storage).WriteToFile(eq(SLOTS_FILE), any());
        }

        @Test
        @DisplayName("adding multiple slot objects accumulates all in the schedule")
        void accumulatesMultipleSlotObjects() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(slotA);
            mgr.addTimeSlot(slotB);

            List<TimeSlot> slots = schedule.getAllSlots();
            assertEquals(2, slots.size());
            assertTrue(slots.contains(slotA));
            assertTrue(slots.contains(slotB));
        }
    }

    // =========================================================================
    // slotExists
    // =========================================================================

    @Nested
    @DisplayName("slotExists")
    class SlotExistsTests {

        @Test
        @DisplayName("returns true when the exact slot is present in the schedule")
        void returnsTrueForExistingSlot() {
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            assertTrue(mgr.slotExists(slotA));
        }

        @Test
        @DisplayName("returns false when the slot is not in the schedule")
        void returnsFalseForAbsentSlot() {
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            assertFalse(mgr.slotExists(slotB));
        }

        @Test
        @DisplayName("returns false when the schedule is empty")
        void returnsFalseWhenScheduleEmpty() {
            ScheduleFileManager mgr = buildManager();

            assertFalse(mgr.slotExists(slotA));
        }

        @Test
        @DisplayName("uses TimeSlot equality — same date and times match regardless of capacity")
        void matchesOnDateAndTimesNotCapacity() {
            // slotA has capacity 1; slotSameTimes has capacity 5 — should still be equal
            TimeSlot slotSameTimes = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), 5);
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            assertTrue(mgr.slotExists(slotSameTimes),
                    "Slots with same date/time but different capacity should be considered equal");
        }
    }

    // =========================================================================
    // hasTimeConflict
    // =========================================================================

    @Nested
    @DisplayName("hasTimeConflict")
    class HasTimeConflictTests {

        @Test
        @DisplayName("returns false when schedule is empty")
        void returnsFalseWhenScheduleEmpty() {
            ScheduleFileManager mgr = buildManager();

            assertFalse(mgr.hasTimeConflict(slotA));
        }

        @Test
        @DisplayName("returns false when new slot is on a different date")
        void returnsFalseForDifferentDate() {
            schedule.addSlot(slotA); // 2026-12-01
            ScheduleFileManager mgr = buildManager();

            // slotC is on 2026-12-02 — no overlap possible
            assertFalse(mgr.hasTimeConflict(slotC));
        }

        @Test
        @DisplayName("returns false when new slot is on the same date but does not overlap")
        void returnsFalseForNonOverlappingSlot() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // 10:00–11:00 starts exactly when slotA ends — no overlap (end is exclusive boundary)
            TimeSlot adjacent = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(10, 0), LocalTime.of(11, 0), 1);

            assertFalse(mgr.hasTimeConflict(adjacent));
        }

        @Test
        @DisplayName("returns true when new slot overlaps with an existing slot")
        void returnsTrueForOverlappingSlot() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // 09:30–10:30 starts inside slotA
            TimeSlot overlapping = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 30), LocalTime.of(10, 30), 1);

            assertTrue(mgr.hasTimeConflict(overlapping));
        }

        @Test
        @DisplayName("returns true when new slot has the same start time as an existing slot")
        void returnsTrueForSameStartTime() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // Same start time, different end — still a conflict
            TimeSlot sameStart = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0), LocalTime.of(9, 30), 1);

            assertTrue(mgr.hasTimeConflict(sameStart));
        }

        @Test
        @DisplayName("returns true when new slot completely contains an existing slot")
        void returnsTrueWhenNewSlotContainsExisting() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // 08:00–11:00 fully contains slotA
            TimeSlot container = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(8, 0), LocalTime.of(11, 0), 1);

            assertTrue(mgr.hasTimeConflict(container));
        }

        @Test
        @DisplayName("returns false for the exact same slot — self-comparison is skipped")
        void returnsFalseForExactDuplicate() {
            schedule.addSlot(slotA);
            ScheduleFileManager mgr = buildManager();

            // slotA is equal to itself — the method skips equal slots
            assertFalse(mgr.hasTimeConflict(slotA));
        }

        @Test
        @DisplayName("boundary: slot ending exactly when another begins is not a conflict")
        void boundaryAdjacentSlotIsNotConflict() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // 08:00–09:00 ends exactly when slotA starts — not a conflict
            TimeSlot endsTouchingStart = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(8, 0), LocalTime.of(9, 0), 1);

            assertFalse(mgr.hasTimeConflict(endsTouchingStart));
        }

        @Test
        @DisplayName("boundary: one-minute overlap is detected as a conflict")
        void boundaryOneMinuteOverlapIsConflict() {
            schedule.addSlot(slotA); // 09:00–10:00
            ScheduleFileManager mgr = buildManager();

            // 09:59–11:00 — one minute of overlap with slotA
            TimeSlot oneMinuteOverlap = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 59), LocalTime.of(11, 0), 1);

            assertTrue(mgr.hasTimeConflict(oneMinuteOverlap));
        }
    }

    // =========================================================================
    // getSchedule and getAllSlots
    // =========================================================================

    @Nested
    @DisplayName("getSchedule and getAllSlots")
    class GettersTests {

        @Test
        @DisplayName("getSchedule returns the same schedule instance passed at construction")
        void getScheduleReturnsSameInstance() {
            ScheduleFileManager mgr = buildManager();

            assertNotNull(mgr.getSchedule());
            assertEquals(schedule, mgr.getSchedule());
        }

        @Test
        @DisplayName("getAllSlots returns empty list when schedule has no slots")
        void getAllSlotsEmptyWhenScheduleEmpty() {
            ScheduleFileManager mgr = buildManager();

            assertNotNull(mgr.getAllSlots());
            assertTrue(mgr.getAllSlots().isEmpty());
        }

        @Test
        @DisplayName("getAllSlots reflects slots added through addTimeSlot")
        void getAllSlotsReflectsAddedSlots() {
            when(storage.WriteToFile(eq(SLOTS_FILE), any())).thenReturn(true);
            ScheduleFileManager mgr = buildManager();

            mgr.addTimeSlot(slotA);
            mgr.addTimeSlot(slotB);

            List<TimeSlot> result = mgr.getAllSlots();
            assertEquals(2, result.size());
            assertTrue(result.contains(slotA));
            assertTrue(result.contains(slotB));
        }
    }
}