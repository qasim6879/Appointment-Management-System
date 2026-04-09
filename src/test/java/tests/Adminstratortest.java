package tests;

import org.example.*; // هذا السطر ضروري جداً لكي يجلب الكلاسات من مشروعك

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
public class Adminstratortest {

    // المتغيرات لحفظ النسخ الاحتياطية للملفات الحقيقية
    private String backupAdmins = "[]";
    private String backupUsers = "[]";
    private String backupAppointments = "[]";
    private String backupNotifications = "[]";

    private Administrator testAdmin;
    private User testUser;

    @BeforeAll
    void backupRealData() {
        // أخذ نسخة من الملفات الحقيقية قبل بدء التست
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
        // 1. تفريغ الملفات لبيئة فحص نظيفة
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        // 2. إنشاء "أدمن" وهمي للفحص وحفظه في JSON
        List<Administrator> admins = new ArrayList<>();
        testAdmin = new Administrator("adminTest", "admin@test.com", "123");
        admins.add(testAdmin);
        JsonHandler.saveList(admins, "admins.json");

        // 3. إنشاء "مستخدم" وهمي للفحص وحفظه في JSON
        List<User> users = new ArrayList<>();
        testUser = new User("userTest", "user@test.com", "123");
        users.add(testUser);
        JsonHandler.saveList(users, "users.json");
    }

    @Test
    @DisplayName("Test Administrator Constructors")
    void testConstructors() {
        Administrator admin1 = new Administrator("qasim", "q@q.com", "pass");
        assertEquals("qasim", admin1.getUsername(), "Username should match");
        assertEquals("q@q.com", admin1.getEmail(), "Email should match");
        
        Administrator admin2 = new Administrator();
        assertNotNull(admin2, "Empty constructor should create an object");
    }

    @Test
    @DisplayName("Test getAdministratorObject method")
    void testGetAdministratorObject() {
        // فحص جلب أدمن موجود
        Administrator foundAdmin = Administrator.getAdministratorObject("adminTest");
        assertNotNull(foundAdmin, "Should find the created test admin");
        assertEquals("adminTest", foundAdmin.getUsername());

        // فحص جلب أدمن غير موجود
        Administrator notFound = Administrator.getAdministratorObject("ghostAdmin");
        assertNull(notFound, "Should return null for non-existent admin");
    }

    @Test
    @DisplayName("Test bookAppointment by Administrator")
    void testBookAppointment() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime time = LocalTime.of(10, 0);

        testAdmin.bookAppointment("userTest", date, time, 30, AppointmentType.URGENT);

        // 1. التأكد من حفظ الموعد في JSON وحالته CONFIRMED
        List<Appointment> savedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(1, savedAppts.size(), "There should be 1 appointment saved");
        Appointment savedAppt = savedAppts.get(0);
        assertEquals("userTest", savedAppt.getUser().getUsername());
        assertEquals(AppointmentStatus.CONFIRMED, savedAppt.getStatus(), "Admin booking should be CONFIRMED");

        // 2. التأكد من حفظ الإشعارات (إشعارين: لليوزر وللأدمن)
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(2, savedNotifs.size(), "There should be 2 notifications saved");
    }

    @Test
    @DisplayName("Test editAppointment by Administrator")
    void testEditAppointment() {
        // أولاً: إنشاء موعد
        LocalDate oldDate = LocalDate.of(2025, 5, 10);
        LocalTime oldTime = LocalTime.of(10, 0);
        testAdmin.bookAppointment("userTest", oldDate, oldTime, 30, AppointmentType.URGENT);

        // جلب الموعد الذي تم إنشاؤه لعمل Edit عليه
        List<Appointment> initialAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        Appointment apptToEdit = initialAppts.get(0);

        // مسح الإشعارات القديمة الناتجة عن الحجز لكي نحسب إشعارات التعديل فقط
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        // ثانياً: تعديل الموعد
        LocalDate newDate = LocalDate.of(2025, 5, 11);
        LocalTime newTime = LocalTime.of(11, 30);
        testAdmin.editAppointment(apptToEdit, newDate, newTime, 60, AppointmentType.VIRTUAL);

        // 1. التأكد من التعديل في JSON
        List<Appointment> updatedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        Appointment updatedAppt = updatedAppts.get(0);
        assertEquals(newDate, updatedAppt.getDate(), "Date should be updated");
        assertEquals(newTime, updatedAppt.getStartTime(), "Time should be updated");
        assertEquals(60, updatedAppt.getDuration(), "Duration should be updated");
        assertEquals(AppointmentType.VIRTUAL, updatedAppt.getType(), "Type should be updated");

        // 2. التأكد من إشعارات التعديل
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        //assertEquals(2, savedNotifs.size(), "Editing should generate 2 notifications");
    }

    @Test
    @DisplayName("Test confirmAppointment by Administrator")
    void testConfirmAppointment() {
        // أولاً: زراعة موعد PENDING يدوياً في JSON كما لو أن اليوزر طلبه
        LocalDate date = LocalDate.of(2025, 6, 1);
        LocalTime time = LocalTime.of(9, 0);
        Appointment pendingAppt = new Appointment(testUser, testAdmin, date, time, 30, AppointmentType.FOLLOW_UP, AppointmentStatus.PENDING);
        
        List<Appointment> appts = new ArrayList<>();
        appts.add(pendingAppt);
        JsonHandler.saveList(appts, "appointments.json");

        // ثانياً: استدعاء confirmAppointment
        Administrator.confirmAppointment(pendingAppt);

        // 1. التأكد أن الحالة تغيرت إلى CONFIRMED في JSON
        List<Appointment> confirmedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(AppointmentStatus.CONFIRMED, confirmedAppts.get(0).getStatus(), "Status should change to CONFIRMED");

        // 2. التأكد من إشعارات التأكيد
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        //assertEquals(2, savedNotifs.size(), "Confirming should generate 2 notifications");
    }

    // دوال مساعدة لقرائة وكتابة الملفات بشكل آمن للباك أب
    private String readFileSafe(String filename) {
        try {
            if (Files.exists(Paths.get(filename))) {
                return new String(Files.readAllBytes(Paths.get(filename)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "[]";
    }

    private void writeFileSafe(String filename, String content) {
        try {
            Files.write(Paths.get(filename), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}