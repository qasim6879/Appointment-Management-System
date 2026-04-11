package tests;

import org.example.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnumsFinalTest {

    @Test
    void testAllEnums() {
        
        for (AppointmentStatus status : AppointmentStatus.values()) {
            assertNotNull(AppointmentStatus.valueOf(status.name()));
        }

        
        for (AppointmentType type : AppointmentType.values()) {
            assertNotNull(AppointmentType.valueOf(type.name()));
        }

        
        for (NotificationType nType : NotificationType.values()) {
            assertNotNull(NotificationType.valueOf(nType.name()));
        }
    }
}