package tests;

import org.example.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppointmentTest {

    private String backupAdmins = "[]";
    private String backupAppointments = "[]";
    private String backupUsers = "[]";

    private User testUser;
    private Administrator testAdmin;

    @BeforeAll
    void backupRealData() {
        backupAdmins = readFileSafe("admins.json");
        backupUsers = readFileSafe("users.json");
        backupAppointments = readFileSafe("appointments.json");
    }

    @AfterAll
    void restoreRealData() {
        writeFileSafe("admins.json", backupAdmins);
        writeFileSafe("users.json", backupUsers);
        writeFileSafe("appointments.json", backupAppointments);
    }

    @BeforeEach
    void setup() {
        // تنظيف الملفات قبل كل تست
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");
        
        testUser = new User("patient1", "p1@test.com", "123");
        testAdmin = new Administrator("doctor1", "d1@test.com", "123");

        List<User> users = new ArrayList<>(); users.add(testUser);
        JsonHandler.saveList(users, "users.json");

        List<Administrator> admins = new ArrayList<>(); admins.add(testAdmin);
        JsonHandler.saveList(admins, "admins.json");
    }

    @Test
    @DisplayName("Test Max Participants based on Appointment Type")
    void testMaxParticipantsLogic() {
        // فحص نوع URGENT (يجب أن يكون 1)
        Appointment appt1 = new Appointment(testUser, testAdmin, LocalDate.now(), LocalTime.of(9, 0), 30, AppointmentType.URGENT, AppointmentStatus.PENDING);
        assertEquals(1, appt1.getMaxParticipants());

        // فحص نوع VIRTUAL (يجب أن يكون 5)
        appt1.setType(AppointmentType.VIRTUAL);
        assertEquals(5, appt1.getMaxParticipants());

        // فحص نوع INDIVIDUAL (يجب أن يكون 1)
        appt1.setType(AppointmentType.INDIVIDUAL);
        assertEquals(1, appt1.getMaxParticipants());
    }

    @Test
    @DisplayName("Test Available Time Slots - Empty Schedule")
    void testAvailableSlotsEmpty() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        boolean[] available = Appointment.availableTimeSlots(date, "doctor1", 30);
        
        // في البداية كل الخانات الـ 12 يجب أن تكون متاحة (true)
        for (boolean slot : available) {
            assertTrue(slot);
        }
    }

    @Test
    @DisplayName("Test Available Time Slots - With Bookings")
    void testAvailableSlotsWithBookings() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        
        // 1. حجز موعد الساعة 9:00 (Index 0) لمدة 30 دقيقة
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment(testUser, testAdmin, date, LocalTime.of(9, 0), 30, AppointmentType.INDIVIDUAL, AppointmentStatus.CONFIRMED));
        JsonHandler.saveList(list, "appointments.json");

        boolean[] available = Appointment.availableTimeSlots(date, "doctor1", 30);
        assertFalse(available[0], "Slot at 9:00 should be busy");
        assertTrue(available[1], "Slot at 9:30 should be free");
    }

    @Test
    @DisplayName("Test 60-minute Appointment impact on slots")
    void test60MinImpact() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        
        // حجز موعد 60 دقيقة يبدأ الساعة 10:00 (Index 2)
        // هذا يعني أنه سيشغل Index 2 و Index 3 (10:00 و 10:30)
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment(testUser, testAdmin, date, LocalTime.of(10, 0), 60, AppointmentType.INDIVIDUAL, AppointmentStatus.CONFIRMED));
        JsonHandler.saveList(list, "appointments.json");

        boolean[] available = Appointment.availableTimeSlots(date, "doctor1", 30);
        assertFalse(available[2], "10:00 should be busy");
        assertFalse(available[3], "10:30 should be busy because the previous appt was 60min");
        assertTrue(available[4], "11:00 should be free");
    }

    @Test
    @DisplayName("Test Cancelled appointments do not block slots")
    void testCancelledApptDoesNotBlock() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        
        // حجز موعد لكن حالته ملغي (CANCELLED)
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment(testUser, testAdmin, date, LocalTime.of(9, 0), 30, AppointmentType.INDIVIDUAL, AppointmentStatus.CANCELLED));
        JsonHandler.saveList(list, "appointments.json");

        boolean[] available = Appointment.availableTimeSlots(date, "doctor1", 30);
        assertTrue(available[0], "Slot should be free because the appointment was cancelled");
    }

    @Test
    @DisplayName("Test requested duration of 60 mins logic")
    void testRequested60MinLogic() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        
        // حجز موعد الساعة 10:30 (Index 3)
        List<Appointment> list = new ArrayList<>();
        list.add(new Appointment(testUser, testAdmin, date, LocalTime.of(10, 30), 30, AppointmentType.INDIVIDUAL, AppointmentStatus.CONFIRMED));
        JsonHandler.saveList(list, "appointments.json");

        // إذا طلب المستخدم موعداً مدته 60 دقيقة
        // الكود لديك في الكلاس يمنع الحجز في الخانة السابقة (Index 2) لكي لا يتداخل موعد الـ 60 دقيقة مع موعد الـ 10:30
        boolean[] available = Appointment.availableTimeSlots(date, "doctor1", 60);
        assertFalse(available[2], "Slot 10:00 should be unavailable if requesting 60 mins because 10:30 is busy");
    }

    // دوال المساعدة
    private String readFileSafe(String filename) {
        try {
            if (Files.exists(Paths.get(filename))) {
                return new String(Files.readAllBytes(Paths.get(filename)));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return "[]";
    }

    private void writeFileSafe(String filename, String content) {
        try {
            Files.write(Paths.get(filename), content.getBytes());
        } catch (IOException e) { e.printStackTrace(); }
    }
}