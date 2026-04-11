package tests;

import org.example.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScheduleTest {

    @Test
    @DisplayName("Test Schedule instantiation")
    void testScheduleCreation() {
        
        
        Schedule schedule = new Schedule();
        assertNotNull(schedule, "Schedule object should be created");
    }
}