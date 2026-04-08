package tests;

import org.example.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationTypeTest {

    @Test
    @DisplayName("Test NotificationType Enum Values")
    void testEnumValues() {
        // التحقق من وجود جميع القيم
        NotificationType[] types = NotificationType.values();
        assertEquals(3, types.length, "Enum should have 3 constants");
        
        assertEquals(NotificationType.REMINDER, NotificationType.valueOf("REMINDER"));
        assertEquals(NotificationType.CONFIRMATION, NotificationType.valueOf("CONFIRMATION"));
        assertEquals(NotificationType.CANCELLATION, NotificationType.valueOf("CANCELLATION"));
    }

    @Test
    @DisplayName("Test Enum constants names")
    void testEnumNames() {
        // التأكد من أن الأسماء لم تتغير (مهم لاستقرار الـ JSON)
        assertEquals("REMINDER", NotificationType.REMINDER.name());
        assertEquals("CONFIRMATION", NotificationType.CONFIRMATION.name());
        assertEquals("CANCELLATION", NotificationType.CANCELLATION.name());
    }
}