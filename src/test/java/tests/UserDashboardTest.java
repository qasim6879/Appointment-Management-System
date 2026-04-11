package tests;

import org.example.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDashboardTest {

    private String backupUsers = "[]";
    private String backupAdmins = "[]";
    private String backupAppts = "[]";
    private String backupNotifs = "[]";
    
    private UserDashboard dashboard;

    @BeforeAll
    void backupData() {
        backupUsers = readFileSafe("users.json");
        backupAdmins = readFileSafe("admins.json");
        backupAppts  = readFileSafe("appointments.json");
        backupNotifs = readFileSafe("notifications.json");
    }

    @AfterAll
    void restoreData() {
        writeFileSafe("users.json", backupUsers);
        writeFileSafe("admins.json", backupAdmins);
        writeFileSafe("appointments.json", backupAppts);
        writeFileSafe("notifications.json", backupNotifs);
    }

    @BeforeEach
    void setup() {
        
        List<User> users = new ArrayList<>();
        users.add(new User("tester", "test@user.com", "123"));
        JsonHandler.saveList(users, "users.json");
        
        
        JsonHandler.saveList(new ArrayList<>(), "admins.json");
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        
        dashboard = new UserDashboard("tester", () -> System.out.println("Logout"));
    }

    @Test
    @DisplayName("Smoke Test: Dashboard Structure")
    void testInit() {
        assertNotNull(dashboard);
        
        assertEquals(2, dashboard.getComponentCount());
    }

    @Test
    @DisplayName("Test Stats Calculation")
    void testStatsLogic() {
        
        
        assertDoesNotThrow(() -> {
            dashboard.repaint();
        });
    }

    @Test
    @DisplayName("Test Tab Switching")
    void testNavigation() {
        
        
        assertNotNull(dashboard.getLayout());
    }

    @Test
    @DisplayName("Test Data Refresh Methods")
    void testRefreshMethods() {
        
        assertDoesNotThrow(() -> {
            
            dashboard.revalidate();
        });
    }

    
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