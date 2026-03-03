package gui;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки сохранения и загрузки конфигурации окон
 */
public class MainApplicationFrameTest {
    private MainApplicationFrame frame;

    @BeforeEach
    void setUp() {
        // Создаем главное окно перед каждым тестом
        frame = new MainApplicationFrame();
    }

    @AfterEach
    void tearDown() {
        // Закрываем окно после каждого теста
        if (frame != null) {
            frame.dispose();
        }
    }

    /**
     * ТЕСТ 1: Проверка создания файла конфигурации
     */
    @Test
    void testSaveWindowPositions_createsFile() throws Exception {
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".robots_window_config.xml");

        if (configFile.exists()) {
            configFile.delete();
        }

        invokeSaveWindowPositions(frame);

        assertTrue(configFile.exists(), "Файл конфигурации должен быть создан");
    }

    /**
     * ТЕСТ 2: Проверка сохранения координат окна
     */
    @Test
    void testSaveWindowPositions_savesCorrectCoordinates() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames(frame);

        if (frames.length > 0) {
            JInternalFrame testFrame = frames[0];

            testFrame.setLocation(123, 456);
            testFrame.setSize(789, 321);

            invokeSaveWindowPositions(frame);

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

    /**
     * ТЕСТ 3: Проверка загрузки координат из файла
     */
    @Test
    void testLoadWindowPositions_loadsCorrectCoordinates() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames(frame);

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

            invokeLoadWindowPositions(frame);

            assertEquals(111, testFrame.getX());
            assertEquals(222, testFrame.getY());
            assertEquals(333, testFrame.getWidth());
            assertEquals(444, testFrame.getHeight());
        }
    }

    /**
     * ТЕСТ 4: Проверка сохранения состояния окна
     */
    @Test
    void testSaveWindowPositions_savesWindowState() throws Exception {
        JInternalFrame[] frames = getDesktopPaneFrames(frame);

        if (frames.length > 0) {
            JInternalFrame testFrame = frames[0];

            boolean wasIcon = testFrame.isIcon();
            boolean wasMaximum = testFrame.isMaximum();

            invokeSaveWindowPositions(frame);

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

    /**
     * ТЕСТ 5: Проверка, что файл создается в домашней папке
     */
    @Test
    void testConfigFileLocation() {
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".robots_window_config.xml");

        assertEquals(userHome, configFile.getParent());
        assertEquals(".robots_window_config.xml", configFile.getName());
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    private void invokeSaveWindowPositions(MainApplicationFrame frame) throws Exception {
        java.lang.reflect.Method method =
                MainApplicationFrame.class.getDeclaredMethod("saveWindowPositions");
        method.setAccessible(true);
        method.invoke(frame);
    }

    private void invokeLoadWindowPositions(MainApplicationFrame frame) throws Exception {
        java.lang.reflect.Method method =
                MainApplicationFrame.class.getDeclaredMethod("loadWindowPositions");
        method.setAccessible(true);
        method.invoke(frame);
    }

    private JInternalFrame[] getDesktopPaneFrames(MainApplicationFrame frame) throws Exception {
        java.lang.reflect.Field field =
                MainApplicationFrame.class.getDeclaredField("desktopPane");
        field.setAccessible(true);
        JDesktopPane desktopPane = (JDesktopPane) field.get(frame);
        return desktopPane.getAllFrames();
    }
}