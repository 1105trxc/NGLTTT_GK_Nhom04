package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Notification;
import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.service.HRQueryService;
import vn.edu.ute.languagecenter.management.ui.gui_finance.AttendancePanel;
import vn.edu.ute.languagecenter.management.ui.gui_finance.EnrollmentPanel;
import vn.edu.ute.languagecenter.management.ui.gui_finance.InvoicePaymentPanel;
import vn.edu.ute.languagecenter.management.ui.gui_finance.ResultPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MainDashboard - Giao diện chính sau khi đăng nhập.
 * Package: gui.operation (skill swing-module-builder - Người 2)
 *
 * Layout: Header (top) | Sidebar (left, 230px) | ContentPanel (center,
 * CardLayout)
 * Phân quyền sidebar theo UserRole.
 */
public class MainDashboard extends JFrame {

    private final UserAccount currentUser;
    private final HRQueryService hrService = new HRQueryService();

    private JPanel contentPanel;
    private CardLayout cardLayout;

    // ─── Tên card ────────────────────────────────────────────────────────────
    public static final String CARD_HOME = "HOME";
    public static final String CARD_NOTIFICATION = "NOTIFICATION";
    public static final String CARD_TEACHER = "TEACHER";
    public static final String CARD_STAFF = "STAFF";
    public static final String CARD_STUDENT = "STUDENT";
    public static final String CARD_BRANCH = "BRANCH";
    public static final String CARD_ACCOUNT = "ACCOUNT";

    // Hệ đào tạo
    public static final String CARD_COURSE = "COURSE";
    public static final String CARD_CLASS = "CLASS";
    public static final String CARD_ROOM = "ROOM";
    public static final String CARD_SCHEDULE = "SCHEDULE";
    public static final String CARD_PLACEMENT = "PLACEMENT";
    public static final String CARD_CERT = "CERTIFICATE";

    // Dịch vụ và tài chính
    public static final String CARD_ENROLLMENT = "ENROLLMENT";
    public static final String CARD_ATTENDANCE = "ATTENDANCE";
    public static final String CARD_RESULT = "RESULT";
    public static final String CARD_INVOICE = "INVOICE";

    // ─── Bảng màu ────────────────────────────────────────────────────────────
    private static final Color C_SIDEBAR = new Color(15, 23, 42); // sidebar đen xanh
    private static final Color C_SIDEBAR_SEC = new Color(30, 41, 59); // hover
    private static final Color C_ACCENT = new Color(59, 130, 246); // xanh sáng (active)
    private static final Color C_HEADER = new Color(15, 23, 42); // header = sidebar
    private static final Color C_BG = new Color(241, 245, 249); // content background
    private static final Color C_WHITE = Color.WHITE;
    private static final Color C_TEXT_MUTED = new Color(148, 163, 184);

    public MainDashboard(UserAccount user) {
        this.currentUser = user;
        initUI();
        showCard(CARD_HOME);
    }

    private void initUI() {
        setTitle("Quản Lý Trung Tâm Ngoại Ngữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        // Sidebar + Content
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildContentPanel(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_HEADER);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        h.setPreferredSize(new Dimension(0, 60));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));
        h.setOpaque(false);

        // Logo + tên hệ thống
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        // Mini logo box
        JPanel logoBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String t = "NN";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoBox.setPreferredSize(new Dimension(36, 36));
        logoBox.setOpaque(false);
        left.add(logoBox);

        JLabel titleLbl = new JLabel("Trung Tâm Ngoại Ngữ");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        left.add(titleLbl);
        h.add(left, BorderLayout.WEST);

        // Thông tin user + Đăng xuất
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        // Avatar + tên
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userInfo.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_SIDEBAR_SEC);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(C_TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String initial = currentUser.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);
        userInfo.add(avatar);

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 0));
        nameBox.setOpaque(false);
        JLabel nameL = new JLabel(currentUser.getUsername());
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameL.setForeground(Color.WHITE);
        JLabel roleL = new JLabel(currentUser.getRole().name());
        roleL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleL.setForeground(C_TEXT_MUTED);
        nameBox.add(nameL);
        nameBox.add(roleL);
        userInfo.add(nameBox);
        right.add(userInfo);

        // Separator
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(new Color(50, 65, 90));
        sep.setPreferredSize(new Dimension(1, 30));
        right.add(sep);

        // Nút logout
        JButton btnOut = new JButton("Đăng xuất") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(220, 38, 38)
                        : new Color(185, 28, 28);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnOut.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOut.setPreferredSize(new Dimension(95, 32));
        btnOut.setContentAreaFilled(false);
        btnOut.setBorderPainted(false);
        btnOut.setFocusPainted(false);
        btnOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOut.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });
        right.add(btnOut);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_SIDEBAR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(230, 0));
        sb.setOpaque(false);
        sb.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Bao bọc JScrollPane để phòng khi menu quá dài
        JPanel innerMenu = new JPanel();
        innerMenu.setLayout(new BoxLayout(innerMenu, BoxLayout.Y_AXIS));
        innerMenu.setOpaque(false);

        // Nhóm điều hướng
        addSidebarSection(innerMenu, "TỔNG QUAN");
        innerMenu.add(menuItem("  Trang Chủ", CARD_HOME, new Color(99, 102, 241)));

        UserAccount.UserRole role = currentUser.getRole();

        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff
                || role == UserAccount.UserRole.Teacher) {
            innerMenu.add(menuItem("  Gửi Thông Báo", CARD_NOTIFICATION, new Color(139, 92, 246)));
        }

        // ═══════════════════════════════════════════════════════════════
        // PHÂN QUYỀN MENU THEO TÀI LIỆU YÊU CẦU:
        // Admin - Toàn quyền tất cả chức năng
        // Staff - Học vụ + Dịch vụ & Tài chính + Nhân sự (trừ Tài khoản)
        // Teacher - Chỉ xem Lớp của mình, Điểm danh, Nhập điểm
        // Student - KHÔNG đăng nhập phần mềm nội bộ
        // ═══════════════════════════════════════════════════════════════

        // ── NGƯỜI 1: HỌC VỤ & ĐÀO TẠO ──────────────────────────────
        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff
                || role == UserAccount.UserRole.Teacher) {
            addSidebarSection(innerMenu, "HỌC VỤ & ĐÀO TẠO");
        }
        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff) {
            innerMenu.add(menuItem("  Khóa Học", CARD_COURSE, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Lớp Học", CARD_CLASS, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Phòng Học", CARD_ROOM, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Lịch Học", CARD_SCHEDULE, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Test Đầu Vào", CARD_PLACEMENT, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Chứng Chỉ", CARD_CERT, new Color(16, 185, 129)));
        } else if (role == UserAccount.UserRole.Teacher) {
            // Giáo viên chỉ thấy lớp mình dạy và lịch dạy
            innerMenu.add(menuItem("  Lớp Của Tôi", CARD_CLASS, new Color(16, 185, 129)));
            innerMenu.add(menuItem("  Lịch Dạy", CARD_SCHEDULE, new Color(16, 185, 129)));
        }

        // ── NGƯỜI 3: DỊCH VỤ & TÀI CHÍNH ───────────────────────────
        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff
                || role == UserAccount.UserRole.Teacher) {
            addSidebarSection(innerMenu, "DỊCH VỤ & TÀI CHÍNH");
        }
        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff) {
            // Admin & Staff quản lý đầy đủ ghi danh, hoá đơn, điểm danh, kết quả
            innerMenu.add(menuItem("  Ghi Danh", CARD_ENROLLMENT, new Color(245, 158, 11)));
            innerMenu.add(menuItem("  Hóa Đơn & TT", CARD_INVOICE, new Color(245, 158, 11)));
            innerMenu.add(menuItem("  Điểm Danh", CARD_ATTENDANCE, new Color(245, 158, 11)));
            innerMenu.add(menuItem("  Kết Quả Học Tập", CARD_RESULT, new Color(245, 158, 11)));
        } else if (role == UserAccount.UserRole.Teacher) {
            // Giáo viên chỉ được điểm danh và nhập kết quả lớp mình dạy
            innerMenu.add(menuItem("  Điểm Danh", CARD_ATTENDANCE, new Color(245, 158, 11)));
            innerMenu.add(menuItem("  Nhập Điểm/KQ", CARD_RESULT, new Color(245, 158, 11)));
        }

        // ── NGƯỜI 2: NHÂN SỰ & HỆ THỐNG ────────────────────────────
        // Student không đăng nhập phần mềm nội bộ → không hiện nhóm này
        if (role == UserAccount.UserRole.Admin || role == UserAccount.UserRole.Staff) {
            addSidebarSection(innerMenu, "NHÂN SỰ & HỆ THỐNG");
            innerMenu.add(menuItem("  Học Viên", CARD_STUDENT, new Color(239, 68, 68)));
            innerMenu.add(menuItem("  Giáo Viên", CARD_TEACHER, new Color(239, 68, 68)));
            innerMenu.add(menuItem("  Chi Nhánh", CARD_BRANCH, new Color(239, 68, 68)));
            if (role == UserAccount.UserRole.Admin) {
                // Chỉ Admin mới được quản lý Nhân viên và Tài khoản hệ thống
                innerMenu.add(menuItem("  Nhân Viên", CARD_STAFF, new Color(239, 68, 68)));
                innerMenu.add(menuItem("  Tài Khoản", CARD_ACCOUNT, new Color(239, 68, 68)));
            }
        }

        innerMenu.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(innerMenu);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // Customize scrollbar if needed or leave it invisible
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        sb.add(scroll);

        // Version info
        JLabel ver = new JLabel("  v1.0  |  Nhóm 04 - UTE");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(new Color(71, 85, 105));
        ver.setAlignmentX(Component.LEFT_ALIGNMENT);
        ver.setBorder(new EmptyBorder(8, 16, 4, 0));
        sb.add(ver);

        return sb;
    }

    private void addSidebarSection(JPanel sb, String title) {
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(16, 0, 6, 0));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        sb.add(lbl);
    }

    private JButton menuItem(String text, String card, Color dotColor) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(C_SIDEBAR_SEC);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                // Chấm màu bên trái
                g2.setColor(dotColor);
                g2.fillOval(16, (getHeight() - 8) / 2, 8, 8);
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                g2.drawString(getText(), 36, (getHeight() + g2.getFontMetrics().getAscent()
                        - g2.getFontMetrics().getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> showCard(card));
        return b;
    }

    // =========================================================================
    // CONTENT PANEL
    // =========================================================================
    private JPanel buildContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(C_BG);

        // Kiem tra neu la giao vien thi lay ID
        Long teacherId = null;
        try {
            if (currentUser.getRole() == UserAccount.UserRole.Teacher && currentUser.getTeacher() != null) {
                teacherId = currentUser.getTeacher().getTeacherId();
            }
        } catch (Exception ex) {
            // Catch lazy loading exceptions if any
            System.err.println("Cannot fetch teacher ID: " + ex.getMessage());
        }

        // HOME
        contentPanel.add(buildHomePanel(), CARD_HOME);

        // THONG BAO
        if (currentUser.getRole() != UserAccount.UserRole.Student) {
            contentPanel.add(new NotificationPanel(currentUser), CARD_NOTIFICATION);
        }

        // NHAN SU
        contentPanel.add(new TeacherPanel(), CARD_TEACHER);
        contentPanel.add(new StaffPanel(), CARD_STAFF);
        contentPanel.add(new StudentPanel(), CARD_STUDENT);
        contentPanel.add(new BranchPanel(), CARD_BRANCH);
        contentPanel.add(new AccountPanel(), CARD_ACCOUNT);

        // HOC VU
        contentPanel.add(new CoursePanel(), CARD_COURSE);
        contentPanel.add(new ClassPanel(), CARD_CLASS);
        contentPanel.add(new RoomPanel(), CARD_ROOM);
        contentPanel.add(new SchedulePanel(), CARD_SCHEDULE);
        contentPanel.add(new PlacementTestPanel(), CARD_PLACEMENT);
        contentPanel.add(new CertificatePanel(), CARD_CERT);

        // DICH VU
        contentPanel.add(new EnrollmentPanel(), CARD_ENROLLMENT);
        contentPanel.add(new AttendancePanel(teacherId), CARD_ATTENDANCE);
        contentPanel.add(new ResultPanel(teacherId), CARD_RESULT);
        contentPanel.add(new InvoicePaymentPanel(), CARD_INVOICE);

        return contentPanel;
    }

    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    // =========================================================================
    // HOME / NOTIFICATION
    // =========================================================================
    private JPanel buildHomePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Greeting bar
        JPanel greet = new JPanel(new BorderLayout());
        greet.setOpaque(false);

        JLabel lbHi = new JLabel("Xin chào, " + currentUser.getUsername() + "!");
        lbHi.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbHi.setForeground(new Color(15, 23, 42));
        greet.add(lbHi, BorderLayout.WEST);

        JLabel lbDate = new JLabel(java.time.LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lbDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbDate.setForeground(new Color(100, 116, 139));
        greet.add(lbDate, BorderLayout.EAST);
        p.add(greet, BorderLayout.NORTH);

        // Role badge
        JPanel mid = new JPanel(new BorderLayout(0, 12));
        mid.setOpaque(false);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        JLabel badge = new JLabel("  " + currentUser.getRole().name() + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(C_ACCENT);
        badge.setBackground(new Color(219, 234, 254));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badgeRow.add(badge);
        mid.add(badgeRow, BorderLayout.NORTH);

        // Notification section
        JLabel sec = new JLabel("Thông Báo Mới Nhất");
        sec.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sec.setForeground(new Color(15, 23, 42));
        sec.setBorder(new EmptyBorder(8, 0, 8, 0));
        mid.add(sec, BorderLayout.CENTER);

        List<Notification> notifs = hrService.getRecentNotificationsForUser(currentUser.getRole());
        JPanel notifList = new JPanel();
        notifList.setLayout(new BoxLayout(notifList, BoxLayout.Y_AXIS));
        notifList.setOpaque(false);

        if (notifs.isEmpty()) {
            JLabel empty = new JLabel("Không có thông báo nào.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(new Color(148, 163, 184));
            notifList.add(empty);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Notification n : notifs) {
                notifList.add(buildNotifCard(n, fmt));
                notifList.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scroll = new JScrollPane(notifList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        mid.add(scroll, BorderLayout.SOUTH);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNotifCard(Notification n, DateTimeFormatter fmt) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Accent bar trái
                g2.setColor(C_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel title = new JLabel(n.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, BorderLayout.NORTH);

        JLabel content = new JLabel("<html><body style='width:100%'>" + n.getContent() + "</body></html>");
        content.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        content.setForeground(new Color(71, 85, 105));
        card.add(content, BorderLayout.CENTER);

        String time = n.getCreatedAt() != null ? n.getCreatedAt().format(fmt) : "";
        JLabel meta = new JLabel(n.getTargetRole() + "  •  " + time);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(new Color(148, 163, 184));
        card.add(meta, BorderLayout.SOUTH);

        return card;
    }

}
