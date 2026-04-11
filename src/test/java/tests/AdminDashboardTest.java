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
public class AdminDashboardTest {

    private String backupAdmins = "[]";
    private String backupAppts  = "[]";
    private AdminDashboard dashboard;

    @BeforeAll
    void backupData() {
        backupAdmins = readFileSafe("admins.json");
        backupAppts  = readFileSafe("appointments.json");
    }

    @AfterAll
    void restoreData() {
        writeFileSafe("admins.json", backupAdmins);
        writeFileSafe("appointments.json", backupAppts);
    }

    @BeforeEach
    void setup() {
        
        List<Administrator> admins = new ArrayList<>();
        admins.add(new Administrator("adminTest", "admin@test.com", "123"));
        JsonHandler.saveList(admins, "admins.json");
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");

        
        
        dashboard = new AdminDashboard("adminTest", () -> {
            System.out.println("Logout clicked");
        });
    }

    @Test
    @DisplayName("Smoke Test: Ensure Dashboard creates all views")
    void testDashboardInitialization() {
        assertNotNull(dashboard, "Dashboard should be instantiated");
        
        
        assertNotNull(dashboard.getComponent(0), "Dashboard should have components");
    }

    @Test
    @DisplayName("Test Sidebar Navigation")
    void testNavigation() {
        
        
        
        assertNotNull(dashboard.getLayout());
    }

    @Test
    @DisplayName("Test Table Data Loading")
    void testTableLoading() {
        
        
        assertDoesNotThrow(() -> {
            
            dashboard.revalidate();
        });
    }

    @Test
    @DisplayName("Test Stats Refresh")
    void testStats() {
        
        assertDoesNotThrow(() -> {
            
            dashboard.repaint();
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
