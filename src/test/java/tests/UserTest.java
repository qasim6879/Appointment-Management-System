package tests;

import org.example.*; // استيراد كلاسات المشروع

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
public class UserTest {

    // المتغيرات لحفظ النسخ الاحتياطية للملفات الحقيقية
    private String backupAdmins = "[]";
    private String backupUsers = "[]";
    private String backupAppointments = "[]";
    private String backupNotifications = "[]";

    private User testUser;
    private Administrator testAdmin;

    @BeforeAll
    void backupRealData() {
        // أخذ نسخة من الملفات الحقيقية قبل بدء التست لحمايتها
        backupAdmins = readFileSafe("admins.json");
        backupUsers = readFileSafe("users.json");
        backupAppointments = readFileSafe("appointments.json");
        backupNotifications = readFileSafe("notifications.json");
    }

    @AfterAll
    void restoreRealData() {
        // إرجاع الملفات الحقيقية بعد انتهاء جميع الفحوصات
        writeFileSafe("admins.json", backupAdmins);
        writeFileSafe("users.json", backupUsers);
        writeFileSafe("appointments.json", backupAppointments);
        writeFileSafe("notifications.json", backupNotifications);
    }

    @BeforeEach
    void setupTestEnvironment() {
        // 1. تفريغ الملفات الحركية لبيئة فحص نظيفة
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        // 2. إنشاء "أدمن" وهمي للفحص
        List<Administrator> admins = new ArrayList<>();
        testAdmin = new Administrator("adminTest", "admin@test.com", "123");
        admins.add(testAdmin);
        JsonHandler.saveList(admins, "admins.json");

        // 3. إنشاء "مستخدم" وهمي للفحص
        List<User> users = new ArrayList<>();
        testUser = new User("userTest", "user@test.com", "123");
        users.add(testUser);
        JsonHandler.saveList(users, "users.json");
    }

    @Test
    @DisplayName("Test User Constructors and Getters/Setters")
    void testConstructorsAndGettersSetters() {
        User user1 = new User("test1", "t1@test.com", "pass1");
        assertEquals("test1", user1.getUsername());
        assertEquals("t1@test.com", user1.getEmail());
        assertEquals("pass1", user1.getPassword());

        User user2 = new User();
        user2.setUsername("test2");
        user2.setEmail("t2@test.com");
        user2.setPassword("pass2");
        
        assertEquals("test2", user2.getUsername());
        assertEquals("t2@test.com", user2.getEmail());
        assertEquals("pass2", user2.getPassword());
    }

    @Test
    @DisplayName("Test getUserObject method")
    void testGetUserObject() {
        User found = User.getUserObject("userTest");
        assertNotNull(found, "Should find the created test user");
        assertEquals("userTest", found.getUsername());

        User notFound = User.getUserObject("ghost");
        assertNull(notFound, "Should return null for non-existent user");
    }

    @Test
    @DisplayName("Test bookAppointment by User")
    void testBookAppointment() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime time = LocalTime.of(10, 0);

        // اليوزر يطلب حجز
        testUser.bookAppointment("adminTest", date, time, 30, AppointmentType.VIRTUAL);

        List<Appointment> appts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(1, appts.size());
        assertEquals(AppointmentStatus.PENDING, appts.get(0).getStatus(), "User booking should be PENDING");

        List<Notification> notifs = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(2, notifs.size(), "Booking should create 2 notifications");
    }

    @Test
    @DisplayName("Test cancelAppointment by User")
    void testCancelAppointment() {
        // إنشاء حجز أولاً
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime time = LocalTime.of(10, 0);
        testUser.bookAppointment("adminTest", date, time, 30, AppointmentType.VIRTUAL);
        
        List<Appointment> appts = JsonHandler.loadList("appointments.json", Appointment.class);
        Appointment apptToCancel = appts.get(0);
        
        // تفريغ الإشعارات الناتجة عن الحجز لكي نفحص إشعارات الإلغاء فقط
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        // اليوزر يلغي الحجز
        testUser.cancelAppointment(apptToCancel);

        List<Appointment> updatedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(AppointmentStatus.CANCELLED, updatedAppts.get(0).getStatus(), "Status should be CANCELLED");

        List<Notification> notifs = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(2, notifs.size(), "Cancellation should create 2 notifications");
    }

    @Test
    @DisplayName("Test getUserAppointments method")
    void testGetUserAppointments() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime time = LocalTime.of(10, 0);
        testUser.bookAppointment("adminTest", date, time, 30, AppointmentType.VIRTUAL);

        ArrayList<Appointment> myAppts = testUser.getUserAppointments();
        assertEquals(1, myAppts.size(), "User should have 1 appointment");
        assertEquals("userTest", myAppts.get(0).getUser().getUsername());
    }

    @Test
    @DisplayName("Test signIn method")
    void testSignIn() {
        assertTrue(User.signIn("userTest", "123", "users.json"), "Valid credentials should return true");
        assertFalse(User.signIn("userTest", "wrongPass", "users.json"), "Wrong password should return false");
        assertFalse(User.signIn("wrongUser", "123", "users.json"), "Wrong username should return false");
    }

    @Test
    @DisplayName("Test signUp method")
    void testSignUp() {
        // فحص إنشاء حساب صحيح (يجب أن يرجع 0)
        int resultValid = User.signUp("newUser", "new@test.com", "pass", "users.json");
        assertEquals(0, resultValid, "Valid signup should return 0");
        assertTrue(User.signIn("newUser", "pass", "users.json"), "New user should be able to sign in");

        // فحص إيميل خاطئ لا يحتوي على @ أو . (يجب أن يرجع 2)
        int resultInvalidEmail = User.signUp("user2", "bademail", "pass", "users.json");
        assertEquals(2, resultInvalidEmail, "Invalid email format should return 2");

        // فحص اسم مستخدم مكرر (يجب أن يرجع 1)
        int resultDuplicateUser = User.signUp("newUser", "other@test.com", "pass", "users.json");
        assertEquals(1, resultDuplicateUser, "Duplicate username should return 1");

        // فحص إيميل مكرر (يجب أن يرجع 2)
        int resultDuplicateEmail = User.signUp("otherUser", "new@test.com", "pass", "users.json");
        assertEquals(2, resultDuplicateEmail, "Duplicate email should return 2");
    }

    // دوال مساعدة لقرائة وكتابة الملفات بشكل آمن للباك أب
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