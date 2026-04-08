package tests;

import org.example.Theme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ThemeTest {

    @Test
    @DisplayName("Test Theme Constants - Colors")
    void testColors() {
        // التأكد من أن الألوان الأساسية ليست Null وأن قيمها صحيحة
        assertNotNull(Theme.INK);
        assertNotNull(Theme.ACCENT);
        assertEquals(Color.WHITE, Theme.WHITE);
        assertEquals(new Color(0x0F, 0x0E, 0x0B), Theme.INK);
    }

    @Test
    @DisplayName("Test Theme Constants - Fonts")
    void testFonts() {
        // التأكد من أن الخطوط تم تعريفها
        assertNotNull(Theme.FONT_TITLE);
        assertNotNull(Theme.FONT_BODY);
        assertEquals(26, Theme.FONT_TITLE.getSize());
        assertTrue(Theme.FONT_TITLE.isBold());
    }

    @Test
    @DisplayName("Test Theme Constants - Spacing")
    void testSpacing() {
        // التأكد من قيم المسافات
        assertEquals(24, Theme.PAD_LG);
        assertEquals(16, Theme.PAD_MD);
        assertEquals(4, Theme.RADIUS);
    }

    @Test
    @DisplayName("Test Private Constructor for 100% Coverage")
    void testPrivateConstructor() throws Exception {
        // هذا الجزء "اختياري" ولكنه السحر الذي سيجعل الكلاس أخضر بالكامل (100%)
        // نستخدم الـ Reflection للوصول للـ Constructor المخفي
        Constructor<Theme> constructor = Theme.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Theme instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
