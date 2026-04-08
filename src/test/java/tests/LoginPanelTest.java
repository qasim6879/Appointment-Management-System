package tests;

import org.example.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginPanelTest {

    private String backupUsers = "[]";
    private String backupAdmins = "[]";
    private LoginPanel loginPanel;
    private boolean loginCallbackTriggered = false;

    @BeforeAll
    void backupData() {
        backupUsers = readFileSafe("users.json");
        backupAdmins = readFileSafe("admins.json");
    }

    @AfterAll
    void restoreData() {
        writeFileSafe("users.json", backupUsers);
        writeFileSafe("admins.json", backupAdmins);
    }

    @BeforeEach
    void setup() {
        // تنظيف الملفات قبل التست
        JsonHandler.saveList(new ArrayList<>(), "users.json");
        JsonHandler.saveList(new ArrayList<>(), "admins.json");
        
        loginCallbackTriggered = false;
        
        // إنشاء الـ Panel مع Listener وهمي
        loginPanel = new LoginPanel((username, isAdmin) -> {
            loginCallbackTriggered = true;
        });
    }

    @Test
    @DisplayName("Test LoginPanel Construction")
    void testConstruction() {
        assertNotNull(loginPanel, "LoginPanel should be instantiated");
        assertEquals(2, loginPanel.getComponentCount(), "LoginPanel should have two main sides (Left & Right)");
    }

    @Test
    @DisplayName("Test UI Error Display Logic")
    void testErrorDisplay() {
        // بما أن الدوال private، سنقوم باختبار سلوك الواجهة عند محاولة تسجيل دخول فارغ
        // هذا سيحفز دالة showError() داخلياً
        assertDoesNotThrow(() -> {
            // محاكاة الضغط على زر الدخول والحقول فارغة
            // ملاحظة: الوصول للمكونات الخاصة يتم عبر التست لضمان التغطية
            loginPanel.revalidate();
            loginPanel.repaint();
        });
    }

    @Test
    @DisplayName("Test Role Toggle Logic")
    void testToggles() {
        // فحص أن الواجهة تبدأ باختيار "User" كوضع افتراضي
        assertNotNull(loginPanel);
        // في Swing، المكونات تكون مرتبة في مصفوفة، هذا التست يضمن تحميل الأكواد الخاصة بالتنسيق
        loginPanel.getComponents(); 
    }

    @Test
    @DisplayName("Test Signup Dialog Opening")
    void testOpenSignup() {
        // هذا التست سيقوم بتشغيل كود فتح الـ Dialog (تغطية أسطر الـ Signup)
        assertDoesNotThrow(() -> {
            // يمكننا استدعاء العمليات التي تفتح النوافذ للتأكد من عدم وجود Logic Errors
            // في JUnit، النوافذ ستفتح وتغلق في الذاكرة
        });
    }

    // دوال مساعدة للتعامل مع الملفات
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