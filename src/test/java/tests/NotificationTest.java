package tests;

import org.example.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationTest {

    private String backupNotifications = "[]";
    private User testUser1;
    private User testUser2;
    private Administrator testAdmin;

    @BeforeAll
    void backupRealData() {
        backupNotifications = readFileSafe("notifications.json");
    }

    @AfterAll
    void restoreRealData() {
        writeFileSafe("notifications.json", backupNotifications);
    }

    @BeforeEach
    void setup() {
        // تنظيف ملف الإشعارات قبل كل فحص
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        testUser1 = new User("user1", "u1@test.com", "123");
        testUser2 = new User("user2", "u2@test.com", "123");
        testAdmin = new Administrator("admin1", "a1@test.com", "123");
    }

    @Test
    @DisplayName("Test Notification Constructor and Date Initialization")
    void testConstructor() {
        Notification note = new Notification("Hello", true, testUser1, testAdmin, NotificationType.CONFIRMATION);
        
        assertNotNull(note.getDateSent(), "Date should be automatically initialized");
        assertEquals("Hello", note.getMessage());
        assertEquals(testUser1.getUsername(), note.getUser().getUsername());
        assertTrue(note.isActive());
    }

    @Test
    @DisplayName("Test getNotifications (Filtering by Username)")
    void testGetNotifications() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification("Msg for User 1", true, testUser1, testAdmin, NotificationType.CONFIRMATION));
        list.add(new Notification("Msg for User 1 again", true, testUser1, testAdmin, NotificationType.REMINDER));
        list.add(new Notification("Msg for User 2", true, testUser2, testAdmin, NotificationType.CONFIRMATION));
        
        JsonHandler.saveList(list, "notifications.json");

        List<Notification> user1Notes = Notification.getNotifications("user1");
        List<Notification> user2Notes = Notification.getNotifications("user2");

        assertEquals(2, user1Notes.size(), "User 1 should have 2 notifications");
        assertEquals(1, user2Notes.size(), "User 2 should have 1 notification");
        assertEquals("user1", user1Notes.get(0).getUser().getUsername());
    }

    @Test
    @DisplayName("Test deleteNotification (Single Deletion)")
    void testDeleteNotification() {
        Notification n1 = new Notification("Message 1", true, testUser1, testAdmin, NotificationType.CONFIRMATION);
        Notification n2 = new Notification("Message 2", true, testUser1, testAdmin, NotificationType.CONFIRMATION);
        
        List<Notification> list = new ArrayList<>();
        list.add(n1);
        list.add(n2);
        JsonHandler.saveList(list, "notifications.json");

        // حذف الإشعار الأول فقط
        Notification.deleteNotification(n1);

        List<Notification> remaining = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(1, remaining.size());
        assertEquals("Message 2", remaining.get(0).getMessage(), "Only Message 2 should remain");
    }

    @Test
    @DisplayName("Test deleteAllNotifications for a specific user")
    void testDeleteAllNotifications() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification("U1 Note A", true, testUser1, testAdmin, NotificationType.CONFIRMATION));
        list.add(new Notification("U1 Note B", true, testUser1, testAdmin, NotificationType.CONFIRMATION));
        list.add(new Notification("U2 Note C", true, testUser2, testAdmin, NotificationType.CONFIRMATION));
        JsonHandler.saveList(list, "notifications.json");

        // حذف كل إشعارات User 1
        Notification.deleteAllNotifications("user1");

        List<Notification> afterDelete = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(1, afterDelete.size(), "Only user2's notification should remain");
        assertEquals("user2", afterDelete.get(0).getUser().getUsername());
    }

    @Test
    @DisplayName("Test deleteNotification with non-existent notification")
    void testDeleteNonExistent() {
        Notification n1 = new Notification("Exist", true, testUser1, testAdmin, NotificationType.CONFIRMATION);
        Notification n2 = new Notification("Not Exist", true, testUser1, testAdmin, NotificationType.CONFIRMATION);
        
        List<Notification> list = new ArrayList<>();
        list.add(n1);
        JsonHandler.saveList(list, "notifications.json");

        // محاولة حذف إشعار غير موجود في الملف
        Notification.deleteNotification(n2);

        List<Notification> remaining = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(1, remaining.size(), "List should remain unchanged");
    }

    // دوال المساعدة للباك أب
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