package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);


        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Загружаем сохраненные позиции окон при запуске
        loadWindowPositions();
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        {
            JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }

        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> {
                Logger.debug("Новая строка");
            });
            testMenu.add(addLogMessageItem);
        }

        // Добавляем разделитель перед пунктом Выход
        testMenu.addSeparator();

        // Пункт меню для выхода
        JMenuItem exitMenuItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitMenuItem.addActionListener((event) -> {
            exitApplication();
        });
        testMenu.add(exitMenuItem);

        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        return menuBar;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

    /**
     * Метод для выхода из приложения с подтверждением
     */
    private void exitApplication() {
        int result = JOptionPane.showOptionDialog(
                this,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Да", "Нет"},
                "Нет"
        );

        if (result == 0) { // Если нажали "Да"
            saveWindowPositions();
            System.exit(0);
        }
    }

    /**
     * Сохраняет позиции и состояние всех внутренних окон в XML-файл
     * в домашней директории пользователя.
     */
    private void saveWindowPositions() {
        try {
            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome, ".robots_window_config.xml");
            Properties props = new Properties();

            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                String frameName = frame.getTitle();

                props.setProperty(frameName + ".x", String.valueOf(frame.getX()));
                props.setProperty(frameName + ".y", String.valueOf(frame.getY()));
                props.setProperty(frameName + ".width", String.valueOf(frame.getWidth()));
                props.setProperty(frameName + ".height", String.valueOf(frame.getHeight()));
                props.setProperty(frameName + ".icon", String.valueOf(frame.isIcon()));
                props.setProperty(frameName + ".maximum", String.valueOf(frame.isMaximum()));
            }

            props.storeToXML(new FileOutputStream(configFile), "Robot Application Window Positions");
            System.out.println("Window positions saved to: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает позиции и состояние окон из XML-файла и применяет их.
     */
    private void loadWindowPositions() {
        try {
            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome, ".robots_window_config.xml");

            if (!configFile.exists()) {
                System.out.println("Config file not found, using default window positions.");
                return;
            }

            Properties props = new Properties();
            props.loadFromXML(new FileInputStream(configFile));

            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                String frameName = frame.getTitle();

                String xStr = props.getProperty(frameName + ".x");
                String yStr = props.getProperty(frameName + ".y");
                if (xStr != null && yStr != null) {
                    int x = Integer.parseInt(xStr);
                    int y = Integer.parseInt(yStr);
                    frame.setLocation(x, y);
                }

                String wStr = props.getProperty(frameName + ".width");
                String hStr = props.getProperty(frameName + ".height");
                if (wStr != null && hStr != null) {
                    int width = Integer.parseInt(wStr);
                    int height = Integer.parseInt(hStr);
                    frame.setSize(width, height);
                }

                String maxStr = props.getProperty(frameName + ".maximum");
                if ("true".equals(maxStr)) {
                    try {
                        frame.setMaximum(true);
                    } catch (Exception e) {
                        // игнорируем
                    }
                }

                String iconStr = props.getProperty(frameName + ".icon");
                if ("true".equals(iconStr) && !"true".equals(maxStr)) {
                    try {
                        frame.setIcon(true);
                    } catch (Exception e) {
                        // игнорируем
                    }
                }
            }

            System.out.println("Window positions loaded from: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}