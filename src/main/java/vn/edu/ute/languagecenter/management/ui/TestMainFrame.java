package vn.edu.ute.languagecenter.management.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Frame test tạm để xem trước các Panel của Thành viên 1.
 * Sau này Thành viên 2 sẽ tích hợp vào Main Dashboard chính thức.
 */
public class TestMainFrame extends JFrame {

    private JPanel contentPanel;

    public TestMainFrame() {
        setTitle("Trung tâm Ngoại ngữ — Preview Thành viên 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Sidebar =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(8, 1, 5, 5));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setBackground(new Color(45, 52, 70));

        String[] menuItems = { "Khóa học", "Lớp học", "Phòng học", "Lịch học", "Test đầu vào", "Chứng chỉ" };
        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setFocusPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(60, 70, 90));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> switchPanel(item));
            sidebar.add(btn);
        }
        add(sidebar, BorderLayout.WEST);

        // ===== Content =====
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel welcome = new JLabel("Chọn chức năng từ menu bên trái", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentPanel.add(welcome, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void switchPanel(String name) {
        contentPanel.removeAll();
        JPanel panel = switch (name) {
            case "Khóa học" -> new CoursePanel();
            case "Lớp học" -> new ClassPanel();
            case "Phòng học" -> new RoomPanel();
            case "Lịch học" -> new SchedulePanel();
            case "Test đầu vào" -> new PlacementTestPanel();
            case "Chứng chỉ" -> new CertificatePanel();
            default -> {
                JPanel p = new JPanel();
                p.add(new JLabel("Chưa có panel cho: " + name));
                yield p;
            }
        };
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new TestMainFrame().setVisible(true);
        });
    }
}
