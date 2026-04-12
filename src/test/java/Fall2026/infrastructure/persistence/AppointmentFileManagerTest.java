package Fall2026.infrastructure.persistence;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.AppointmentType;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AppointmentFileManager.
 *
 * Mock strategy:
 * - CredentialStorage: always mocked — performs real file I/O.
 * - Schedule: always mocked — owns slot state, is an external dependency.
 * - Appointment, TimeSlot: pure domain classes, always instantiated directly.
 *
 * ⚠ Requires one source change to be testable:
 *   Add a package-private constructor to AppointmentFileManager that accepts
 *   both dependencies so the mocks can be injected:
 *
 *     AppointmentFileManager(Schedule schedule, CredentialStorage storage) {
 *         this.schedule = schedule;
 *         this.storage  = storage;
 *     }
 *
 *   The existing public constructor is unchanged for production use.
 */
@DisplayName("AppointmentFileManager Tests")
class AppointmentFileManagerTest {

    private static final String APPOINTMENTS_FILE = "Appointments.txt";

    // ─── Mocked dependencies ─────────────────────────────────────────────────

    @Mock
    private Schedule schedule;

    @Mock
    private CredentialStorage storage;

    // ─── Shared fixtures ─────────────────────────────────────────────────────

    private AppointmentFileManager manager;

    private TimeSlot futureSlot;        // 2026-12-01, 09:00–10:00, capacity 2
    private TimeSlot anotherSlot;       // 2026-12-02, 11:00–12:00, capacity 1
    private Appointment confirmedAppt;  // IN_PERSON, CONFIRMED, 1 booking
    private Appointment pendingAppt;    // GROUP,     PENDING,    0 bookings

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        futureSlot   = new TimeSlot(LocalDate.of(2026, 12, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
        anotherSlot  = new TimeSlot(LocalDate.of(2026, 12, 2),
                LocalTime.of(11, 0), LocalTime.of(12, 0), 1);

        confirmedAppt = new Appointment(futureSlot, "Checkup", 2, AppointmentType.IN_PERSON);
        confirmedAppt.addBooking();

        pendingAppt = new Appointment(anotherSlot, "Group Session", 20, AppointmentType.GROUP);
        pendingAppt.setStatus(Appointment.AppointmentStatus.PENDING);

        manager = new AppointmentFileManager(schedule, storage);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        reset(schedule, storage);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Builds the 7-field CSV line exactly as AppointmentFileManager writes it.
     */
    private String apptLine(TimeSlot slot, AppointmentType type,
                            Appointment.AppointmentStatus status,
                            String description, int bookings) {
        return slot.getDate()      + ","
                + slot.getStartTime() + ","
                + slot.getEndTime()   + ","
                + type                + ","
                + status              + ","
                + description         + ","
                + bookings;
    }

    // =========================================================================
    // saveAppointmentsToFile
    // =========================================================================

    @Nested
    @DisplayName("saveAppointmentsToFile")
    class SaveAppointmentsToFileTests {

        @Test
        @DisplayName("writes a single appointment as a correctly formatted CSV line")
        void writesSingleAppointmentCorrectly() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);

            manager.saveAppointmentsToFile(List.of(confirmedAppt));

            String expectedLine = apptLine(
                    futureSlot,
                    AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED,
                    "Checkup",
                    1
            );

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), captor.capture());

            List<String> written = captor.getValue();
            assertEquals(1, written.size());
            assertEquals(expectedLine, written.get(0));
        }

        @Test
        @DisplayName("writes multiple appointments as separate CSV lines")
        void writesMultipleAppointments() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);

            manager.saveAppointmentsToFile(List.of(confirmedAppt, pendingAppt));

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), captor.capture());

            List<String> written = captor.getValue();
            assertEquals(2, written.size());
            assertEquals(
                    apptLine(futureSlot, AppointmentType.IN_PERSON,
                            Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1),
                    written.get(0)
            );
            assertEquals(
                    apptLine(anotherSlot, AppointmentType.GROUP,
                            Appointment.AppointmentStatus.PENDING, "Group Session", 0),
                    written.get(1)
            );
        }

        @Test
        @DisplayName("writes an empty list when no appointments exist")
        void writesEmptyListWhenNoAppointments() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);

            manager.saveAppointmentsToFile(new ArrayList<>());

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), captor.capture());

            assertTrue(captor.getValue().isEmpty());
        }

        @Test
        @DisplayName("records zero bookings correctly for an unbooked appointment")
        void recordsZeroBookingsForUnbookedAppointment() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);
            Appointment unbooked = new Appointment(
                    futureSlot, "Empty", 2, AppointmentType.VIRTUAL);

            manager.saveAppointmentsToFile(List.of(unbooked));

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), captor.capture());

            String line = captor.getValue().get(0);
            assertTrue(line.endsWith(",0"), "Line should end with ',0' for zero bookings");
        }

        @Test
        @DisplayName("records boundary booking count equal to max capacity")
        void recordsFullCapacityBookings() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);
            Appointment full = new Appointment(futureSlot, "Full", 2, AppointmentType.IN_PERSON);
            full.addBooking();
            full.addBooking(); // now at capacity of 2

            manager.saveAppointmentsToFile(List.of(full));

            ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), captor.capture());

            String line = captor.getValue().get(0);
            assertTrue(line.endsWith(",2"), "Line should end with ',2' at full capacity");
        }

        @Test
        @DisplayName("delegates to storage even when WriteToFile returns false")
        void delegatesToStorageEvenOnWriteFailure() {
            when(storage.WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(false);

            // Should not throw — failure is logged, not propagated
            manager.saveAppointmentsToFile(List.of(confirmedAppt));

            verify(storage).WriteToFile(eq(APPOINTMENTS_FILE), org.mockito.ArgumentMatchers.any());
        }
    }

    // =========================================================================
    // loadAppointmentsFromFile
    // =========================================================================

    @Nested
    @DisplayName("loadAppointmentsFromFile")
    class LoadAppointmentsFromFileTests {

        @Test
        @DisplayName("returns empty list when file has no lines")
        void returnsEmptyListWhenFileIsEmpty() {
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(new ArrayList<>());

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("loads a single valid appointment line and restores all fields")
        void loadsSingleValidAppointment() {
            String line = apptLine(futureSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            Appointment loaded = result.get(0);
            assertEquals(futureSlot,                              loaded.getTimeSlot());
            assertEquals(AppointmentType.IN_PERSON,              loaded.getAppointmentType());
            assertEquals(Appointment.AppointmentStatus.CONFIRMED, loaded.getStatus());
            assertEquals("Checkup",                               loaded.getDescription());
            assertEquals(1,                                       loaded.getCurrentBookings());
        }

        @Test
        @DisplayName("loads multiple valid appointment lines correctly")
        void loadsMultipleValidAppointments() {
            String line1 = apptLine(futureSlot,  AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            String line2 = apptLine(anotherSlot, AppointmentType.GROUP,
                    Appointment.AppointmentStatus.PENDING, "Group Session", 0);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line1, line2));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot, anotherSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("restores multiple bookings by calling addBooking the correct number of times")
        void restoresBookingCountCorrectly() {
            TimeSlot twoCapSlot = new TimeSlot(LocalDate.of(2026, 12, 3),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), 2);
            String line = apptLine(twoCapSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Double", 2);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(twoCapSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getCurrentBookings());
        }

        @Test
        @DisplayName("restores zero bookings for an unbooked appointment")
        void restoresZeroBookingsCorrectly() {
            String line = apptLine(futureSlot, AppointmentType.VIRTUAL,
                    Appointment.AppointmentStatus.CONFIRMED, "Virtual", 0);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getCurrentBookings());
        }

        @Test
        @DisplayName("skips line that has fewer than 7 comma-separated parts")
        void skipsLineTooFewParts() {
            // Only 6 fields — missing bookings count
            String badLine = "2026-12-01,09:00,10:00,IN_PERSON,CONFIRMED,Checkup";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(badLine));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips line when no matching slot exists in the schedule")
        void skipsLineWhenNoMatchingSlot() {
            String line = apptLine(futureSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(new ArrayList<>()); // no slots registered

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips line with an unrecognised AppointmentType enum value")
        void skipsLineWithUnknownType() {
            String badLine = "2026-12-01,09:00,10:00,UNKNOWN_TYPE,CONFIRMED,Checkup,1";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(badLine));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips line with an unrecognised AppointmentStatus enum value")
        void skipsLineWithUnknownStatus() {
            String badLine = "2026-12-01,09:00,10:00,IN_PERSON,INVALID_STATUS,Checkup,1";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(badLine));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("skips line with a non-integer booking count")
        void skipsLineWithNonIntegerBookings() {
            String badLine = "2026-12-01,09:00,10:00,IN_PERSON,CONFIRMED,Checkup,abc";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(badLine));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("loads valid lines and skips invalid ones in the same file")
        void loadsValidAndSkipsInvalidLines() {
            String goodLine = apptLine(futureSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            String badLine  = "garbage,data,here";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(goodLine, badLine));
            when(schedule.getAllSlots()).thenReturn(List.of(futureSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            assertEquals(futureSlot, result.get(0).getTimeSlot());
        }

        @Test
        @DisplayName("matches slot by date, start time, and end time — ignores capacity")
        void matchesSlotByDateAndTimes() {
            // Schedule has a slot with capacity 5; file line was saved when capacity was 2
            TimeSlot scheduledSlot = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), 5);
            String line = apptLine(futureSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(scheduledSlot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            // Should match on date+start+end regardless of capacity difference
            assertEquals(1, result.size());
            assertEquals(scheduledSlot, result.get(0).getTimeSlot());
        }

        @Test
        @DisplayName("description containing commas is preserved correctly with split limit of 7")
        void preservesDescriptionWithCommas() {
            // "Consultation, follow-up" contains a comma — split(",", 7) must handle this
            TimeSlot slot = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0), LocalTime.of(10, 0), 1);
            String line = "2026-12-01,09:00,10:00,IN_PERSON,CONFIRMED,Consultation follow-up,1";
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(slot));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            assertEquals("Consultation follow-up", result.get(0).getDescription());
        }
    }

    // =========================================================================
    // findSlot (tested indirectly via loadAppointmentsFromFile)
    // =========================================================================

    @Nested
    @DisplayName("findSlot — via loadAppointmentsFromFile")
    class FindSlotTests {

        @Test
        @DisplayName("returns null when schedule has no slots at all")
        void returnsNullWhenScheduleEmpty() {
            String line = apptLine(futureSlot, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 1);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(new ArrayList<>());

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertTrue(result.isEmpty(), "Appointment should be skipped when no slot matches");
        }

        @Test
        @DisplayName("matches only the slot whose date, start, and end all agree")
        void matchesOnlyExactSlot() {
            TimeSlot wrongDate  = new TimeSlot(LocalDate.of(2026, 12, 9),
                    LocalTime.of(9, 0),  LocalTime.of(10, 0), 1);
            TimeSlot wrongStart = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(8, 0),  LocalTime.of(10, 0), 1);
            TimeSlot correct    = new TimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(9, 0),  LocalTime.of(10, 0), 1);

            String line = apptLine(correct, AppointmentType.IN_PERSON,
                    Appointment.AppointmentStatus.CONFIRMED, "Checkup", 0);
            when(storage.ReadFromFile(APPOINTMENTS_FILE)).thenReturn(List.of(line));
            when(schedule.getAllSlots()).thenReturn(List.of(wrongDate, wrongStart, correct));

            List<Appointment> result = manager.loadAppointmentsFromFile();

            assertEquals(1, result.size());
            assertEquals(correct, result.get(0).getTimeSlot());
        }
    }
}