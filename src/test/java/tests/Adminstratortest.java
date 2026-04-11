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
public class Adminstratortest {

    
    private String backupAdmins = "[]";
    private String backupUsers = "[]";
    private String backupAppointments = "[]";
    private String backupNotifications = "[]";

    private Administrator testAdmin;
    private User testUser;

    @BeforeAll
    void backupRealData() {
        
        backupAdmins = readFileSafe("admins.json");
        backupUsers = readFileSafe("users.json");
        backupAppointments = readFileSafe("appointments.json");
        backupNotifications = readFileSafe("notifications.json");
    }

    @AfterAll
    void restoreRealData() {
        
        writeFileSafe("admins.json", backupAdmins);
        writeFileSafe("users.json", backupUsers);
        writeFileSafe("appointments.json", backupAppointments);
        writeFileSafe("notifications.json", backupNotifications);
    }

    @BeforeEach
    void setupTestEnvironment() {
        
        JsonHandler.saveList(new ArrayList<>(), "appointments.json");
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        
        List<Administrator> admins = new ArrayList<>();
        testAdmin = new Administrator("adminTest", "admin@test.com", "123");
        admins.add(testAdmin);
        JsonHandler.saveList(admins, "admins.json");

        
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
        
        Administrator foundAdmin = Administrator.getAdministratorObject("adminTest");
        assertNotNull(foundAdmin, "Should find the created test admin");
        assertEquals("adminTest", foundAdmin.getUsername());

        
        Administrator notFound = Administrator.getAdministratorObject("ghostAdmin");
        assertNull(notFound, "Should return null for non-existent admin");
    }

    @Test
    @DisplayName("Test bookAppointment by Administrator")
    void testBookAppointment() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        LocalTime time = LocalTime.of(10, 0);

        testAdmin.bookAppointment("userTest", date, time, 30, AppointmentType.URGENT);

        
        List<Appointment> savedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(1, savedAppts.size(), "There should be 1 appointment saved");
        Appointment savedAppt = savedAppts.get(0);
        assertEquals("userTest", savedAppt.getUser().getUsername());
        assertEquals(AppointmentStatus.CONFIRMED, savedAppt.getStatus(), "Admin booking should be CONFIRMED");

        
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        assertEquals(2, savedNotifs.size(), "There should be 2 notifications saved");
    }

    @Test
    @DisplayName("Test editAppointment by Administrator")
    void testEditAppointment() {
        
        LocalDate oldDate = LocalDate.of(2025, 5, 10);
        LocalTime oldTime = LocalTime.of(10, 0);
        testAdmin.bookAppointment("userTest", oldDate, oldTime, 30, AppointmentType.URGENT);

        
        List<Appointment> initialAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        Appointment apptToEdit = initialAppts.get(0);

        
        JsonHandler.saveList(new ArrayList<>(), "notifications.json");

        
        LocalDate newDate = LocalDate.of(2025, 5, 11);
        LocalTime newTime = LocalTime.of(11, 30);
        testAdmin.editAppointment(apptToEdit, newDate, newTime, 60, AppointmentType.VIRTUAL);

        
        List<Appointment> updatedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        Appointment updatedAppt = updatedAppts.get(0);
        assertEquals(newDate, updatedAppt.getDate(), "Date should be updated");
        assertEquals(newTime, updatedAppt.getStartTime(), "Time should be updated");
        assertEquals(60, updatedAppt.getDuration(), "Duration should be updated");
        assertEquals(AppointmentType.VIRTUAL, updatedAppt.getType(), "Type should be updated");

        
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        
    }

    @Test
    @DisplayName("Test confirmAppointment by Administrator")
    void testConfirmAppointment() {
        
        LocalDate date = LocalDate.of(2025, 6, 1);
        LocalTime time = LocalTime.of(9, 0);
        Appointment pendingAppt = new Appointment(testUser, testAdmin, date, time, 30, AppointmentType.FOLLOW_UP, AppointmentStatus.PENDING);
        
        List<Appointment> appts = new ArrayList<>();
        appts.add(pendingAppt);
        JsonHandler.saveList(appts, "appointments.json");

        
        Administrator.confirmAppointment(pendingAppt);

        
        List<Appointment> confirmedAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        assertEquals(AppointmentStatus.CONFIRMED, confirmedAppts.get(0).getStatus(), "Status should change to CONFIRMED");

        
        List<Notification> savedNotifs = JsonHandler.loadList("notifications.json", Notification.class);
        
    }

    
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