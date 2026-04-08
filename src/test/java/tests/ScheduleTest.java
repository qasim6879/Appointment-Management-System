package tests;

import org.example.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleTest {

    @Test
    @DisplayName("Test Schedule instantiation")
    void testScheduleCreation() {
        // بما أن الكلاس لا يحتوي إلا على متغير private
        // سنختبر فقط القدرة على إنشاء كائن منه
        Schedule schedule = new Schedule();
        assertNotNull(schedule, "Schedule object should be created");
    }
}