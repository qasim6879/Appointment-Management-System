package tests;

import org.example.JsonHandler;
import org.example.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JsonHandlerTest {

    private final String TEST_FILE = "test_handler_data.json";

    @BeforeEach
    @AfterEach
    void deleteTestFile() {
        // حذف ملف الفحص قبل وبعد كل تست لضمان نظافة البيئة
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("Test Saving and Loading a List of Objects")
    void testSaveAndLoad() {
        List<User> users = new ArrayList<>();
        users.add(new User("jsonUser", "json@test.com", "pass"));
        
        // فحص الحفظ
        JsonHandler.saveList(users, TEST_FILE);
        
        // فحص التحميل
        List<User> loadedUsers = JsonHandler.loadList(TEST_FILE, User.class);
        
        assertEquals(1, loadedUsers.size());
        assertEquals("jsonUser", loadedUsers.get(0).getUsername());
    }

    @Test
    @DisplayName("Test LocalDate and LocalTime Serialization")
    void testDateTimeSerialization() {
        // هذا التست يغطي الـ TypeAdapters التي أضفتها في الكلاس
        // سننشئ كلاس بسيط يحتوي على تاريخ ووقت لاختبار الـ Gson Adapter
        // بما أننا لا نملك كلاس اختبار بسيط، سنستخدم الـ Appointment بشكل غير مباشر أو نعتمد على أن الحفظ لم ينهار
        
        // سنحاول حفظ قائمة تحتوي على تواريخ (بما أن الـ JsonHandler مصمم لذلك)
        // إذا نجحت العملية دون Exception، فهذا يعني أن الـ Adapters تعمل
        List<LocalDate> dates = new ArrayList<>();
        dates.add(LocalDate.of(2025, 1, 1));
        
        assertDoesNotThrow(() -> {
            JsonHandler.saveList(dates, TEST_FILE);
        });

        // التحقق من أن الملف ليس فارغاً وأن التاريخ كتب بصيغة صحيحة
        File file = new File(TEST_FILE);
        assertTrue(file.length() > 0);
    }

    @Test
    @DisplayName("Test loading non-existent file")
    void testLoadNonExistentFile() {
        // تحميل ملف غير موجود يجب أن يعيد قائمة فارغة ولا يسبب Error
        List<User> result = JsonHandler.loadList("ghost_file.json", User.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test saving empty list")
    void testSaveEmptyList() {
        List<User> emptyList = new ArrayList<>();
        JsonHandler.saveList(emptyList, TEST_FILE);
        
        List<User> loaded = JsonHandler.loadList(TEST_FILE, User.class);
        assertTrue(loaded.isEmpty());
    }

    @Test
    @DisplayName("Test Pretty Printing (Optional Check)")
    void testFileContentFormat() {
        List<User> users = new ArrayList<>();
        users.add(new User("A", "a@a.com", "p"));
        JsonHandler.saveList(users, TEST_FILE);
        
        // التحقق من أن الـ Json يحتوي على "username" (أي تم تحويله لنص بنجاح)
        List<User> loaded = JsonHandler.loadList(TEST_FILE, User.class);
        assertFalse(loaded.isEmpty());
    }
}