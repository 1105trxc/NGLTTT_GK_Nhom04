package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.service.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;

/**
 * LoginForm - Giao diện đăng nhập được thiết kế lại với gradient background,
 * card hiệu ứng bo góc, và layout cân đối.
 * Package: gui.operation (skill swing-module-builder - Người 2)
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private final UserService userService = new UserService();

    // ─── Bảng màu ────────────────────────────────────────────────────────────
    private static final Color C_PRIMARY = new Color(26, 86, 219); // xanh đậm
    private static final Color C_PRIMARY_DK = new Color(17, 62, 162); // hover
    private static final Color C_BG_TOP = new Color(30, 58, 138); // gradient đầu
    private static final Color C_BG_BOT = new Color(67, 97, 238); // gradient cuối
    private static final Color C_CARD = Color.WHITE;
    private static final Color C_TEXT = new Color(31, 41, 55);
    private static final Color C_HINT = new Color(107, 114, 128);
    private static final Color C_BORDER = new Color(209, 213, 219);
    private static final Color C_FOCUS = new Color(147, 197, 253);
    private static final Color C_ERROR = new Color(220, 38, 38);
    private static final Color C_SUCCESS = new Color(22, 163, 74);

    public LoginForm() {
        initUI();
    }

    private void initUI() {
        setTitle("Đăng Nhập - Hệ Thống Quản Lý Trung Tâm Ngoại Ngữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        // ─── Outer panel: vẽ gradient background ─────────────────────────────
        JPanel outer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, C_BG_TOP,
                        getWidth(), getHeight(), C_BG_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        setContentPane(outer);

        // ─── Card trắng bo góc ────────────────────────────────────────────────
        JPanel card = new JPanel(null) { // null layout để tự đặt vị trí
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Đổ bóng nhẹ (vẽ trước nền)
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 8, getHeight() - 6, 20, 20));
                // Nền trắng
                g2.setColor(C_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(380, 470));
        outer.add(card);

        // ─── Logo + Tiêu đề ──────────────────────────────────────────────────
        JLabel lblLogo = new JLabel("TTNN", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBackground(C_PRIMARY);
        lblLogo.setOpaque(true);
        lblLogo.setBounds(155, 40, 70, 70);
        // Tạo logo tròn
        lblLogo.setBorder(new EmptyBorder(0, 0, 0, 0));
        lblLogo.putClientProperty("arc", 35);
        card.add(makeLogo(155, 40));

        JLabel lblTitle = new JLabel("Trung Tâm Ngoại Ngữ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(C_TEXT);
        lblTitle.setBounds(20, 125, 340, 30);
        card.add(lblTitle);

        JLabel lblSub = new JLabel("Đăng nhập vào hệ thống quản lý", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(C_HINT);
        lblSub.setBounds(20, 158, 340, 20);
        card.add(lblSub);

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(229, 231, 235));
        sep.setBounds(30, 190, 320, 2);
        card.add(sep);

        // ─── Label + Field: Tên đăng nhập ────────────────────────────────────
        JLabel lUser = makeLabel("Tên đăng nhập");
        lUser.setBounds(30, 205, 320, 20);
        card.add(lUser);

        txtUsername = makeTextField();
        txtUsername.setBounds(30, 228, 320, 42);
        card.add(txtUsername);

        // ─── Label + Field: Mật khẩu ─────────────────────────────────────────
        JLabel lPwd = makeLabel("Mật khẩu");
        lPwd.setBounds(30, 282, 320, 20);
        card.add(lPwd);

        txtPassword = new JPasswordField();
        styleInputField(txtPassword);
        txtPassword.setBounds(30, 305, 320, 42);
        card.add(txtPassword);

        // ─── Nút Đăng nhập ───────────────────────────────────────────────────
        btnLogin = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? C_PRIMARY_DK : C_PRIMARY;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setBounds(30, 365, 320, 46);
        card.add(btnLogin);

        // ─── Label trạng thái ────────────────────────────────────────────────
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(C_ERROR);
        lblStatus.setBounds(20, 420, 340, 20);
        card.add(lblStatus);

        // ─── Footer ───────────────────────────────────────────────────────────
        JLabel lblFooter = new JLabel("© 2025 Nhóm 04 - UTE", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(180, 190, 210));
        lblFooter.setBounds(0, 510, 480, 20);
        outer.setLayout(new GridBagLayout());
        // (footer nằm ngoài card, thêm riêng bằng cách thêm vào outer với constraint)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(12, 0, 0, 0);
        outer.add(lblFooter, gbc);

        // ─── Sự kiện ─────────────────────────────────────────────────────────
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        // Focus border effect
        FocusListener fb = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(C_FOCUS, 2),
                                new EmptyBorder(4, 12, 4, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                ((JComponent) e.getSource()).setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(C_BORDER, 1),
                                new EmptyBorder(4, 12, 4, 12)));
            }
        };
        txtUsername.addFocusListener(fb);
        txtPassword.addFocusListener(fb);
    }

    /** Logo tròn màu brand */
    private JPanel makeLogo(int x, int y) {
        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient tròn
                GradientPaint gp = new GradientPaint(0, 0, C_PRIMARY,
                        getWidth(), getHeight(), new Color(99, 102, 241));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                // Chữ
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String t = "NN";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setBounds(x, y, 70, 70);
        return logo;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(55, 65, 81));
        return l;
    }

    private JTextField makeTextField() {
        JTextField f = new JTextField();
        styleInputField(f);
        return f;
    }

    private void styleInputField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1),
                new EmptyBorder(4, 12, 4, 12)));
        f.setBackground(new Color(249, 250, 251));
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pwd = new String(txtPassword.getPassword());

        if (user.isEmpty() || pwd.isEmpty()) {
            showStatus("Vui lòng nhập đầy đủ thông tin!", C_ERROR);
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText("Đang xác thực...");
        lblStatus.setForeground(C_HINT);

        // Chạy login ở background thread để không đơ UI
        SwingWorker<Optional<UserAccount>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<UserAccount> doInBackground() {
                return userService.login(user, pwd);
            }

            @Override
            protected void done() {
                try {
                    Optional<UserAccount> result = get();
                    if (result.isPresent()) {
                        showStatus("Đăng nhập thành công! Đang tải...", C_SUCCESS);
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new MainDashboard(result.get()).setVisible(true);
                        });
                    } else {
                        showStatus("Sai tên đăng nhập hoặc mật khẩu!", C_ERROR);
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    showStatus("Lỗi kết nối: " + ex.getMessage(), C_ERROR);
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void showStatus(String msg, Color color) {
        lblStatus.setForeground(color);
        lblStatus.setText(msg);
    }
}
