package tests;

import org.example.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotTest {

    @Test
    @DisplayName("Test TimeSlot Constructor and Getters")
    void testTimeSlotData() {
        LocalDate date = LocalDate.of(2025, 12, 25);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime durationAsTime = LocalTime.of(11, 0); // لاحظنا أن المتغير من نوع LocalTime

        TimeSlot slot = new TimeSlot(date, start, durationAsTime);

        // فحص صحة تخزين واسترجاع البيانات
        assertEquals(date, slot.getDate(), "Date should match");
        assertEquals(start, slot.getStartTime(), "Start time should match");
        assertEquals(durationAsTime, slot.getEndTime(), "End time (duration field) should match");
    }

    @Test
    @DisplayName("Test TimeSlot with different values")
    void testTimeSlotEdgeValues() {
        // فحص قيم مختلفة للتأكد من أن الكلاس مرن
        TimeSlot slot = new TimeSlot(null, null, null);
        
        assertNull(slot.getDate());
        assertNull(slot.getStartTime());
        assertNull(slot.getEndTime());
    }
}