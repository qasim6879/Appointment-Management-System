package tests;

import org.example.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnumsFinalTest {

    @Test
    void testAllEnums() {
        // تغطية AppointmentStatus
        for (AppointmentStatus status : AppointmentStatus.values()) {
            assertNotNull(AppointmentStatus.valueOf(status.name()));
        }

        // تغطية AppointmentType
        for (AppointmentType type : AppointmentType.values()) {
            assertNotNull(AppointmentType.valueOf(type.name()));
        }

        // تغطية NotificationType
        for (NotificationType nType : NotificationType.values()) {
            assertNotNull(NotificationType.valueOf(nType.name()));
        }
    }
}