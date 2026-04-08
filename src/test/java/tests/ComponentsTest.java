package tests;

import org.example.Components;
import org.example.Theme;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentsTest {

    @Test
    @DisplayName("Test Labels creation and styling")
    void testLabels() {
        JLabel title = Components.title("Hello Title");
        assertEquals("Hello Title", title.getText());
        assertEquals(Theme.FONT_TITLE, title.getFont());

        JLabel section = Components.sectionLabel("settings");
        assertEquals("SETTINGS", section.getText()); // التأكد من تحويل النص لـ CAPS
        assertEquals(Theme.ACCENT, section.getForeground());
    }

    @Test
    @DisplayName("Test Button Factory Methods")
    void testButtons() {
        JButton primary = Components.primaryBtn("Click Me");
        assertNotNull(primary);
        assertEquals(Theme.WHITE, primary.getForeground());
        assertTrue(primary instanceof Components.RoundButton);

        JButton danger = Components.dangerBtn("Delete");
        assertEquals(Theme.ACCENT, danger.getForeground());
    }

    @Test
    @DisplayName("Test Sidebar logic")
    void testSidebar() {
        JButton sidebarBtn = Components.sidebarItem("🏠", "Home");
        assertNotNull(sidebarBtn);
        
        // فحص تفعيل الزر
        Components.setSidebarActive(sidebarBtn);
        assertEquals(Boolean.TRUE, sidebarBtn.getClientProperty("active"));
        assertEquals(Theme.ACCENT, sidebarBtn.getBackground());
    }

    @Test
    @DisplayName("Test Input Fields")
    void testInputs() {
        JTextField tf = Components.textField("Enter name");
        assertEquals(Theme.PAPER, tf.getBackground());
        
        JPasswordField pf = Components.passwordField();
        assertNotNull(pf);
    }

    @Test
    @DisplayName("Test Status Tags")
    void testTags() {
        JLabel confirmed = Components.tagConfirmed();
        assertEquals(Theme.SUCCESS, confirmed.getForeground());
        
        JLabel urgent = Components.tagUrgent();
        assertEquals(Theme.ACCENT, urgent.getForeground());
    }

    @Test
    @DisplayName("Test Panel Factories")
    void testPanels() {
        JPanel card = Components.card();
        assertEquals(Theme.CARD, card.getBackground());

        JPanel stat = Components.statCard("100", "Total", Color.RED);
        assertNotNull(stat);
    }

    @Test
    @DisplayName("Test Table Styling")
    void testTableStyle() {
        JTable table = new JTable();
        Components.styleTable(table);
        assertEquals(Theme.CARD, table.getBackground());
        assertFalse(table.getTableHeader().getReorderingAllowed());
    }

    @Test
    @DisplayName("Test Private Constructor and Inner Class")
    void testInternalStructure() throws Exception {
        // لتغطية الباني الخاص بالـ Components
        Constructor<Components> constructor = Components.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());

        // فحص الـ RoundButton
        Components.RoundButton rb = new Components.RoundButton("Test", Color.BLACK, Color.WHITE);
        rb.setBorderColor(Color.RED);
        assertNotNull(rb);
    }
}