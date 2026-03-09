package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Notification;
import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaNotificationRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationPanel extends JPanel {

    private final JpaNotificationRepository notificationRepo = new JpaNotificationRepository();
    private final UserAccount currentUser;

    private JTextField txtTitle;
    private JTextArea txtContent;
    private JComboBox<Notification.TargetRole> cmbTargetRole;
    private JButton btnSend;

    private static final Color COLOR_BG = new Color(245, 247, 250);

    public NotificationPanel(UserAccount currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lbl = new JLabel("📢  Gửi Thông Báo");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(25, 55, 95));
        panel.add(lbl, BorderLayout.WEST);

        return panel;
    }

    private JPanel buildCenter() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(30, 40, 30, 40)));

        JLabel lblTitle = new JLabel("Tiêu đề thông báo (*)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTitle);
        form.add(Box.createVerticalStrut(5));

        txtTitle = new JTextField();
        styleTextField(txtTitle);
        form.add(txtTitle);
        form.add(Box.createVerticalStrut(20));

        JLabel lblRole = new JLabel("Gửi đến đối tượng (*)");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblRole);
        form.add(Box.createVerticalStrut(5));

        cmbTargetRole = new JComboBox<>(getAllowedRoles());
        cmbTargetRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTargetRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmbTargetRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cmbTargetRole);
        form.add(Box.createVerticalStrut(20));

        JLabel lblContent = new JLabel("Nội dung thông báo (*)");
        lblContent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblContent);
        form.add(Box.createVerticalStrut(5));

        txtContent = new JTextArea(10, 20);
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(txtContent);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 210), 1));
        form.add(scroll);
        form.add(Box.createVerticalStrut(20));

        btnSend = new JButton("Gửi Thông Báo");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setBackground(new Color(34, 139, 34));
        btnSend.setForeground(Color.WHITE);
        btnSend.setOpaque(true);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setMaximumSize(new Dimension(150, 40));
        btnSend.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSend.addActionListener(e -> sendNotification());

        form.add(btnSend);

        // Wrapper to keep form centered somewhat
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(form, BorderLayout.NORTH);

        return wrapper;
    }

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private Notification.TargetRole[] getAllowedRoles() {
        UserAccount.UserRole role = currentUser.getRole();
        List<Notification.TargetRole> targetRoles = new ArrayList<>();

        if (role == UserAccount.UserRole.Admin) {
            targetRoles.add(Notification.TargetRole.Staff);
            targetRoles.add(Notification.TargetRole.Teacher);
            targetRoles.add(Notification.TargetRole.Student);
            targetRoles.add(Notification.TargetRole.All);
        } else if (role == UserAccount.UserRole.Staff) {
            targetRoles.add(Notification.TargetRole.Teacher);
            targetRoles.add(Notification.TargetRole.Student);
        } else if (role == UserAccount.UserRole.Teacher) {
            targetRoles.add(Notification.TargetRole.Student);
        }

        return targetRoles.toArray(new Notification.TargetRole[0]);
    }

    private void sendNotification() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        Notification.TargetRole targetRole = (Notification.TargetRole) cmbTargetRole.getSelectedItem();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (targetRole == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng nhận!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn gửi thông báo này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                Notification notif = new Notification();
                notif.setTitle(title);
                notif.setContent(content);
                notif.setTargetRole(targetRole);
                notif.setCreatedByUser(currentUser);
                notif.setCreatedAt(LocalDateTime.now());

                notificationRepo.save(notif);

                JOptionPane.showMessageDialog(this, "Gửi thông báo thành công!");
                txtTitle.setText("");
                txtContent.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi gửi thông báo: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
