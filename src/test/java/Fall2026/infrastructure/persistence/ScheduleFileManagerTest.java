//package Fall2026.infrastructure.persistence;
//
//import Fall2026.domain.appointment.Schedule;
//import Fall2026.domain.appointment.TimeSlot;
//import Fall2026.domain.exceptions.ValidationException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ScheduleFileManagerTest {
//    private MockScheduleFileManager scheduleFileManager;
//    private Schedule schedule;
//    private TimeSlot slot1;
//    private TimeSlot slot2;
//    private TimeSlot slot3;
//
//    @BeforeEach
//    void setUp() {
//        // Create fresh schedule and manager for each test
//        schedule = new Schedule();
//        scheduleFileManager = new MockScheduleFileManager(schedule);
//
//        // Create test time slots
//        slot1 = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        slot2 = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(10, 0),
//            LocalTime.of(11, 0)
//        );
//
//        slot3 = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(14, 0),
//            LocalTime.of(15, 0)
//        );
//    }
//
//    @AfterEach
//    void tearDown() {
//        // Cleanup after each test
//        if (schedule != null) {
//            schedule = null;
//        }
//    }
//
//    // ========== saveSlotsToFile() Tests ==========
//
//    @Test
//    void saveSlotsToFileWithEmptySchedule() {
//        // Arrange
//        assertTrue(schedule.getAllSlots().isEmpty());
//
//        // Act & Assert - should not throw
//        assertDoesNotThrow(() -> {
//            scheduleFileManager.saveSlotsToFile();
//        });
//    }
//
//    @Test
//    void saveSlotsToFileWithSingleSlot() {
//        // Arrange
//        schedule.addSlot(slot1);
//        assertEquals(1, schedule.getAllSlots().size());
//
//        // Act & Assert - should not throw
//        assertDoesNotThrow(() -> {
//            scheduleFileManager.saveSlotsToFile();
//        });
//    }
//
//    @Test
//    void saveSlotsToFileWithMultipleSlots() {
//        // Arrange
//        schedule.addSlot(slot1);
//        schedule.addSlot(slot2);
//        schedule.addSlot(slot3);
//        assertEquals(3, schedule.getAllSlots().size());
//
//        // Act & Assert - should not throw
//        assertDoesNotThrow(() -> {
//            scheduleFileManager.saveSlotsToFile();
//        });
//    }
//
//    // ========== addTimeSlot(LocalDate, LocalTime, LocalTime) Tests ==========
//
//    @Test
//    void addTimeSlotWithValidParameters() {
//        // Arrange
//        LocalDate date = LocalDate.of(2026, 4, 15);
//        LocalTime startTime = LocalTime.of(10, 0);
//        LocalTime endTime = LocalTime.of(11, 0);
//        int initialSize = schedule.getAllSlots().size();
//
//        // Act
//        scheduleFileManager.addTimeSlot(date, startTime, endTime);
//
//        // Assert
//        assertEquals(initialSize + 1, schedule.getAllSlots().size());
//
//        TimeSlot addedSlot = schedule.getAllSlots().get(schedule.getAllSlots().size() - 1);
//        assertEquals(date, addedSlot.getDate());
//        assertEquals(startTime, addedSlot.getStartTime());
//        assertEquals(endTime, addedSlot.getEndTime());
//    }
//
//    @Test
//    void addTimeSlotWithInvalidTimes() {
//        // Act & Assert - end time before start time
//        assertThrows(ValidationException.class, () -> {
//            scheduleFileManager.addTimeSlot(
//                LocalDate.of(2026, 4, 15),
//                LocalTime.of(11, 0),
//                LocalTime.of(10, 0)
//            );
//        });
//    }
//
//    @Test
//    void addTimeSlotWithSameStartAndEndTime() {
//        // Act & Assert
//        assertThrows(ValidationException.class, () -> {
//            scheduleFileManager.addTimeSlot(
//                LocalDate.of(2026, 4, 15),
//                LocalTime.of(10, 0),
//                LocalTime.of(10, 0)
//            );
//        });
//    }
//
//    @Test
//    void addMultipleTimeSlotsWithDifferentDates() {
//        // Arrange
//        LocalDate date1 = LocalDate.of(2026, 4, 15);
//        LocalDate date2 = LocalDate.of(2026, 4, 16);
//
//        // Act
//        scheduleFileManager.addTimeSlot(date1, LocalTime.of(9, 0), LocalTime.of(10, 0));
//        scheduleFileManager.addTimeSlot(date2, LocalTime.of(9, 0), LocalTime.of(10, 0));
//
//        // Assert
//        assertEquals(2, schedule.getAllSlots().size());
//    }
//
//    // ========== addTimeSlot(TimeSlot) Tests ==========
//
//    @Test
//    void addTimeSlotObjectSuccessfully() {
//        // Arrange
//        int initialSize = schedule.getAllSlots().size();
//
//        // Act
//        scheduleFileManager.addTimeSlot(slot1);
//
//        // Assert
//        assertEquals(initialSize + 1, schedule.getAllSlots().size());
//        assertTrue(schedule.getAllSlots().contains(slot1));
//    }
//
//    @Test
//    void addTimeSlotObjectMultipleTimes() {
//        // Arrange & Act
//        scheduleFileManager.addTimeSlot(slot1);
//        scheduleFileManager.addTimeSlot(slot2);
//        scheduleFileManager.addTimeSlot(slot3);
//
//        // Assert
//        assertEquals(3, schedule.getAllSlots().size());
//        assertTrue(schedule.getAllSlots().contains(slot1));
//        assertTrue(schedule.getAllSlots().contains(slot2));
//        assertTrue(schedule.getAllSlots().contains(slot3));
//    }
//
//    @Test
//    void addTimeSlotObjectAllowsDuplicates() {
//        // Arrange
//        scheduleFileManager.addTimeSlot(slot1);
//
//        // Act - Adding duplicate
//        scheduleFileManager.addTimeSlot(slot1);
//
//        // Assert - Both are added (no duplicate checking in addTimeSlot)
//        assertEquals(2, schedule.getAllSlots().size());
//    }
//
//    // ========== slotExists() Tests ==========
//
//    @Test
//    void slotExistsReturnsFalseForEmptySchedule() {
//        // Act
//        boolean exists = scheduleFileManager.slotExists(slot1);
//
//        // Assert
//        assertFalse(exists);
//    }
//
//    @Test
//    void slotExistsReturnsTrueForExistingSlot() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Act
//        boolean exists = scheduleFileManager.slotExists(slot1);
//
//        // Assert
//        assertTrue(exists);
//    }
//
//    @Test
//    void slotExistsReturnsFalseForNonExistentSlot() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Act
//        boolean exists = scheduleFileManager.slotExists(slot2);
//
//        // Assert
//        assertFalse(exists);
//    }
//
//    @Test
//    void slotExistsWithMultipleSlotsOnSameDate() {
//        // Arrange
//        schedule.addSlot(slot1);
//        schedule.addSlot(slot2);
//        schedule.addSlot(slot3);
//
//        // Act & Assert
//        assertTrue(scheduleFileManager.slotExists(slot1));
//        assertTrue(scheduleFileManager.slotExists(slot2));
//        assertTrue(scheduleFileManager.slotExists(slot3));
//    }
//
//    @Test
//    void slotExistsUsesEqualsMethod() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Create identical slot with different object reference
//        TimeSlot identicalSlot = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        // Act
//        boolean exists = scheduleFileManager.slotExists(identicalSlot);
//
//        // Assert
//        assertTrue(exists); // Should be true because equals() is used
//    }
//
//    // ========== hasTimeConflict() Tests ==========
//
//    @Test
//    void hasTimeConflictReturnsFalseForEmptySchedule() {
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(slot1);
//
//        // Assert
//        assertFalse(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsTrueForExactDuplicate() {
//        // Arrange
//        schedule.addSlot(slot1);
//        TimeSlot duplicate = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(duplicate);
//
//        // Assert
//        assertTrue(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsTrueForOverlappingSlots() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//
//        TimeSlot overlapping = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 30), // Overlaps with slot1
//            LocalTime.of(10, 30)
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(overlapping);
//
//        // Assert
//        assertTrue(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsTrueForPartialOverlap() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//
//        TimeSlot partial = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 45), // Starts within slot1
//            LocalTime.of(11, 0)
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(partial);
//
//        // Assert
//        assertTrue(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsTrueForSameStartTime() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//
//        TimeSlot sameStart = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 0), // Same start as slot1
//            LocalTime.of(11, 0)
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(sameStart);
//
//        // Assert
//        assertTrue(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsFalseForAdjacentSlots() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//        schedule.addSlot(slot2); // 10:00-11:00 (adjacent, not overlapping)
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(slot2);
//
//        // Assert
//        assertFalse(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsFalseForDifferentDates() {
//        // Arrange
//        schedule.addSlot(slot1); // 2026-04-10, 9:00-10:00
//
//        TimeSlot differentDate = new TimeSlot(
//            LocalDate.of(2026, 4, 11), // Different date
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(differentDate);
//
//        // Assert
//        assertFalse(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictWithMultipleSlotsOnSameDate() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//        schedule.addSlot(slot3); // 14:00-15:00
//
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(13, 0),
//            LocalTime.of(14, 30) // Overlaps with slot3
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(newSlot);
//
//        // Assert
//        assertTrue(hasConflict);
//    }
//
//    @Test
//    void hasTimeConflictReturnsFalseForSafeSlot() {
//        // Arrange
//        schedule.addSlot(slot1); // 9:00-10:00
//        schedule.addSlot(slot2); // 10:00-11:00
//        schedule.addSlot(slot3); // 14:00-15:00
//
//        TimeSlot safeSlot = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(11, 0),
//            LocalTime.of(12, 0) // No conflicts
//        );
//
//        // Act
//        boolean hasConflict = scheduleFileManager.hasTimeConflict(safeSlot);
//
//        // Assert
//        assertFalse(hasConflict);
//    }
//
//    // ========== getSchedule() Tests ==========
//
//    @Test
//    void getScheduleReturnsValidScheduleObject() {
//        // Act
//        Schedule returnedSchedule = scheduleFileManager.getSchedule();
//
//        // Assert
//        assertNotNull(returnedSchedule);
//        assertSame(schedule, returnedSchedule);
//    }
//
//    @Test
//    void getScheduleReturnsScheduleWithAddedSlots() {
//        // Arrange
//        schedule.addSlot(slot1);
//        schedule.addSlot(slot2);
//
//        // Act
//        Schedule returnedSchedule = scheduleFileManager.getSchedule();
//
//        // Assert
//        assertEquals(2, returnedSchedule.getAllSlots().size());
//        assertTrue(returnedSchedule.getAllSlots().contains(slot1));
//        assertTrue(returnedSchedule.getAllSlots().contains(slot2));
//    }
//
//    // ========== getAllSlots() Tests ==========
//
//    @Test
//    void getAllSlotsReturnsEmptyListForEmptySchedule() {
//        // Act
//        List<TimeSlot> slots = scheduleFileManager.getAllSlots();
//
//        // Assert
//        assertNotNull(slots);
//        assertTrue(slots.isEmpty());
//    }
//
//    @Test
//    void getAllSlotsReturnsSingleSlot() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Act
//        List<TimeSlot> slots = scheduleFileManager.getAllSlots();
//
//        // Assert
//        assertEquals(1, slots.size());
//        assertTrue(slots.contains(slot1));
//    }
//
//    @Test
//    void getAllSlotsReturnsMultipleSlots() {
//        // Arrange
//        schedule.addSlot(slot1);
//        schedule.addSlot(slot2);
//        schedule.addSlot(slot3);
//
//        // Act
//        List<TimeSlot> slots = scheduleFileManager.getAllSlots();
//
//        // Assert
//        assertEquals(3, slots.size());
//        assertTrue(slots.contains(slot1));
//        assertTrue(slots.contains(slot2));
//        assertTrue(slots.contains(slot3));
//    }
//
//    @Test
//    void getAllSlotsReturnsNewListInstance() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Act
//        List<TimeSlot> slots1 = scheduleFileManager.getAllSlots();
//        List<TimeSlot> slots2 = scheduleFileManager.getAllSlots();
//
//        // Assert - Different list objects
//        assertNotSame(slots1, slots2);
//        // But same contents
//        assertEquals(slots1, slots2);
//    }
//
//    @Test
//    void getAllSlotsIsNotModifiable() {
//        // Arrange
//        schedule.addSlot(slot1);
//
//        // Act
//        List<TimeSlot> slots = scheduleFileManager.getAllSlots();
//
//        // Try to modify returned list
//        TimeSlot newSlot = new TimeSlot(
//            LocalDate.of(2026, 4, 20),
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        // This should not affect the schedule
//        slots.add(newSlot);
//
//        // Assert - Schedule should still have only 1 slot
//        assertEquals(1, schedule.getAllSlots().size());
//    }
//
//    // ========== Constructor Tests ==========
//
//    @Test
//    void constructorWithExistingScheduleUsesProvidedSchedule() {
//        // Arrange
//        Schedule existingSchedule = new Schedule();
//        existingSchedule.addSlot(slot1);
//
//        // Act
//        MockScheduleFileManager manager = new MockScheduleFileManager(existingSchedule);
//
//        // Assert
//        assertNotNull(manager);
//        assertSame(existingSchedule, manager.getSchedule());
//        assertTrue(manager.getSchedule().getAllSlots().contains(slot1));
//    }
//
//    @Test
//    void constructorInitializesScheduleObject() {
//        // Act
//        Schedule testSchedule = new Schedule();
//        MockScheduleFileManager manager = new MockScheduleFileManager(testSchedule);
//
//        // Assert
//        assertNotNull(manager.getSchedule());
//        assertSame(testSchedule, manager.getSchedule());
//    }
//
//    // ========== Integration Tests ==========
//
//    @Test
//    void completeWorkflow() {
//        // Arrange & Act
//        scheduleFileManager.addTimeSlot(slot1);
//        scheduleFileManager.addTimeSlot(slot3);
//
//        // Assert slots exist
//        assertTrue(scheduleFileManager.slotExists(slot1));
//        assertTrue(scheduleFileManager.slotExists(slot3));
//        assertEquals(2, scheduleFileManager.getAllSlots().size());
//
//        // Assert no conflict between them
//        assertFalse(scheduleFileManager.hasTimeConflict(slot1));
//        assertFalse(scheduleFileManager.hasTimeConflict(slot3));
//
//        // Create conflicting slot and verify conflict detection
//        TimeSlot conflicting = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 30),
//            LocalTime.of(10, 30)
//        );
//        assertTrue(scheduleFileManager.hasTimeConflict(conflicting));
//
//        // Save and verify
//        assertDoesNotThrow(() -> scheduleFileManager.saveSlotsToFile());
//    }
//
//    @Test
//    void workflowWithSlotReplacement() {
//        // Add initial slot
//        scheduleFileManager.addTimeSlot(slot1);
//        assertEquals(1, scheduleFileManager.getAllSlots().size());
//
//        // Try to add slot with same time (different object)
//        TimeSlot duplicate = new TimeSlot(
//            LocalDate.of(2026, 4, 10),
//            LocalTime.of(9, 0),
//            LocalTime.of(10, 0)
//        );
//
//        // Should detect conflict
//        assertTrue(scheduleFileManager.hasTimeConflict(duplicate));
//
//        // Can still add it (no prevention in addTimeSlot)
//        scheduleFileManager.addTimeSlot(duplicate);
//        assertEquals(2, scheduleFileManager.getAllSlots().size());
//    }
//
//    /**
//     * Mock implementation of ScheduleFileManager that prevents file I/O during tests.
//     * Standalone class that wraps Schedule without inheriting file I/O side effects.
//     */
//    private static class MockScheduleFileManager {
//        private Schedule schedule;
//
//        public MockScheduleFileManager(Schedule existingSchedule) {
//            this.schedule = existingSchedule;
//        }
//
//        public void saveSlotsToFile() {
//            // Mock implementation - don't write to file
//            // This prevents side effects during testing
//        }
//
//        public void addTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
//            TimeSlot slot = new TimeSlot(date, startTime, endTime);
//            schedule.addSlot(slot);
//        }
//
//        public void addTimeSlot(TimeSlot slot) {
//            schedule.addSlot(slot);
//        }
//
//        public boolean slotExists(TimeSlot slot) {
//            for (TimeSlot existing : schedule.getAllSlots()) {
//                if (existing.equals(slot)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        public boolean hasTimeConflict(TimeSlot slot) {
//            for (TimeSlot existing : schedule.getAllSlots()) {
//                // Skip self-comparison
//                if (existing.equals(slot)) {
//                    continue;
//                }
//                // Only check slots on the same date
//                if (existing.getDate().equals(slot.getDate())) {
//                    // Check for time range overlap:
//                    // Conflict exists if: newSlot.start < existing.end AND newSlot.end > existing.start
//                    if (slot.getStartTime().isBefore(existing.getEndTime()) &&
//                        slot.getEndTime().isAfter(existing.getStartTime())) {
//                        return true;
//                    }
//                    // Also check for exact same start time (even if end times differ)
//                    if (slot.getStartTime().equals(existing.getStartTime())) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//
//        public Schedule getSchedule() {
//            return schedule;
//        }
//
//        public java.util.List<TimeSlot> getAllSlots() {
//            return schedule.getAllSlots();
//        }
//    }
//}