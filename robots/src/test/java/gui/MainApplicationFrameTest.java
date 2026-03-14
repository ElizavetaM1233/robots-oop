package gui;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class MainApplicationFrameTest {
    private MainApplicationFrame frame;

    @BeforeEach
    void setUp() {
        frame = new MainApplicationFrame();
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            frame.dispose();
        }

        // Удаляем тестовый файл
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".robots_window_config.xml");
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    @Test
    void testSaveWindowPositions_createsFile() throws Exception {
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".robots_window_config.xml");

        if (configFile.exists()) {
            configFile.delete();
        }

        java.lang.reflect.Method method =
                MainApplicationFrame.class.getDeclaredMethod("saveWindowPositions");
        method.setAccessible(true);
        method.invoke(frame);

        assertTrue(configFile.exists(), "Файл конфигурации должен быть создан");
    }

    @Test
    void testSaveWindowPositions_savesCorrectCoordinates() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames();

        if (frames.length > 0) {
            JInternalFrame testFrame = frames[0];

            testFrame.setLocation(123, 456);
            testFrame.setSize(789, 321);

            java.lang.reflect.Method saveMethod =
                    MainApplicationFrame.class.getDeclaredMethod("saveWindowPositions");
            saveMethod.setAccessible(true);
            saveMethod.invoke(frame);

            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome, ".robots_window_config.xml");

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.loadFromXML(fis);
            }

            String frameName = testFrame.getTitle();

            assertEquals("123", props.getProperty(frameName + ".x"));
            assertEquals("456", props.getProperty(frameName + ".y"));
            assertEquals("789", props.getProperty(frameName + ".width"));
            assertEquals("321", props.getProperty(frameName + ".height"));
        }
    }

    @Test
    void testLoadWindowPositions_loadsCorrectCoordinates() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames();

        if (frames.length > 0) {
            JInternalFrame testFrame = frames[0];
            String frameName = testFrame.getTitle();

            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome, ".robots_window_config.xml");

            Properties props = new Properties();
            props.setProperty(frameName + ".x", "111");
            props.setProperty(frameName + ".y", "222");
            props.setProperty(frameName + ".width", "333");
            props.setProperty(frameName + ".height", "444");
            props.setProperty(frameName + ".maximum", "false");
            props.setProperty(frameName + ".icon", "false");

            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.storeToXML(fos, "Test config");
            }

            testFrame.setLocation(999, 999);

            java.lang.reflect.Method loadMethod =
                    MainApplicationFrame.class.getDeclaredMethod("loadWindowPositions");
            loadMethod.setAccessible(true);
            loadMethod.invoke(frame);

            assertEquals(111, testFrame.getX());
            assertEquals(222, testFrame.getY());
            assertEquals(333, testFrame.getWidth());
            assertEquals(444, testFrame.getHeight());
        }
    }

    @Test
    void testSaveWindowPositions_savesWindowState() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames();

        if (frames.length > 0) {
            JInternalFrame testFrame = frames[0];

            boolean wasIcon = testFrame.isIcon();
            boolean wasMaximum = testFrame.isMaximum();

            java.lang.reflect.Method saveMethod =
                    MainApplicationFrame.class.getDeclaredMethod("saveWindowPositions");
            saveMethod.setAccessible(true);
            saveMethod.invoke(frame);

            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome, ".robots_window_config.xml");

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.loadFromXML(fis);
            }

            String frameName = testFrame.getTitle();

            assertEquals(String.valueOf(wasIcon), props.getProperty(frameName + ".icon"));
            assertEquals(String.valueOf(wasMaximum), props.getProperty(frameName + ".maximum"));
        }
    }

    @Test
    void testConfigFileLocation() {
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".robots_window_config.xml");

        assertEquals(userHome, configFile.getParent());
        assertEquals(".robots_window_config.xml", configFile.getName());
    }

    private JInternalFrame[] getDesktopPaneFrames() throws Exception {
        java.lang.reflect.Field field =
                MainApplicationFrame.class.getDeclaredField("desktopPane");
        field.setAccessible(true);
        JDesktopPane desktopPane = (JDesktopPane) field.get(frame);
        return desktopPane.getAllFrames();
    }
}