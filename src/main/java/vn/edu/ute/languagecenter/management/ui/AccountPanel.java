package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.*;
import vn.edu.ute.languagecenter.management.service.UserService;
import vn.edu.ute.languagecenter.management.repo.jpa.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AccountPanel — Quản lý tài khoản hệ thống.
 * - Admin: tạo không cần liên kết
 * - Teacher/Staff/Student: dropdown chọn người (tuân thủ DB constraint)
 * - loadData() KHÔNG truy cập lazy proxy → không LazyInitializationException
 */
public class AccountPanel extends JPanel {

    private final UserService userService = new UserService();
    private final JpaTeacherRepository teacherDAO = new JpaTeacherRepository();
    private final JpaStaffRepository staffDAO = new JpaStaffRepository();
    private final JpaStudentRepository studentDAO = new JpaStudentRepository();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    private JTextField txtUsername;
    private JPasswordField pwdPassword;
    private JComboBox<UserAccount.UserRole> cmbRole;

    // Liên kết — ẩn/hiện theo role
    private JPanel linkedPanel;
    private JLabel lblLinked;
    private JComboBox<String> cmbLinked;
    private List<Teacher> teachers;
    private List<Staff> staffList;
    private List<Student> students;

    private JButton btnAdd, btnChangePwd, btnToggleLock, btnClear;
    private Long selectedAccountId = null;

    private static final Color COLOR_PRIMARY = new Color(30, 78, 128);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    public AccountPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        // Load dữ liệu người dùng trước khi build form
        teachers = teacherDAO.findAll();
        staffList = staffDAO.findAll();
        students = studentDAO.findAll();

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);
        loadData();
    }

    // ──────────────────────────────────── TOP BAR
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lbl = new JLabel("🔐  Quản Lý Tài Khoản");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(25, 55, 95));
        panel.add(lbl, BorderLayout.WEST);

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sp.setOpaque(false);
        txtSearch = new JTextField(18);
        styleField(txtSearch);
        JButton btnSearch = new JButton("🔍 Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleBtn(btnSearch, COLOR_PRIMARY);
        styleBtn(btnRefresh, new Color(80, 120, 80));
        btnSearch.addActionListener(e -> searchData());
        txtSearch.addActionListener(e -> searchData());
        btnRefresh.addActionListener(e -> loadData());
        sp.add(new JLabel("Tìm: "));
        sp.add(txtSearch);
        sp.add(btnSearch);
        sp.add(btnRefresh);
        panel.add(sp, BorderLayout.EAST);
        return panel;
    }

    // ──────────────────────────────────── TABLE
    private JScrollPane buildCenter() {
        // Bảng chỉ hiện Username, Vai Trò, Trạng Thái — KHÔNG đụng lazy proxy
        String[] cols = { "ID", "Username", "Vai Trò", "Trạng Thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(COLOR_PRIMARY);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(20, 55, 100)));
                setOpaque(true);
                return this;
            }
        });
        table.setSelectionBackground(new Color(200, 220, 245));
        table.setGridColor(new Color(220, 225, 235));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillForm();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));
        return scroll;
    }

    // ──────────────────────────────────── FORM
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(20, 20, 20, 20)));
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel ttl = new JLabel("Thông Tin Tài Khoản");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ttl.setForeground(COLOR_PRIMARY);
        ttl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(ttl);
        panel.add(Box.createVerticalStrut(14));

        // Username
        addLabel(panel, "Tên đăng nhập (*)");
        txtUsername = new JTextField();
        styleField(txtUsername);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(8));

        // Password
        addLabel(panel, "Mật khẩu (*)");
        pwdPassword = new JPasswordField();
        pwdPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pwdPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pwdPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdPassword.setBackground(Color.WHITE);
        pwdPassword.setForeground(new Color(30, 30, 30));
        pwdPassword.setCaretColor(COLOR_PRIMARY);
        pwdPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        panel.add(pwdPassword);
        panel.add(Box.createVerticalStrut(8));

        // Role
        addLabel(panel, "Vai trò");
        cmbRole = new JComboBox<>(UserAccount.UserRole.values());
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbRole.setBackground(Color.WHITE);
        cmbRole.addActionListener(e -> updateLinkedPanel());
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(8));

        // Panel liên kết (ẩn/hiện theo role)
        linkedPanel = new JPanel();
        linkedPanel.setLayout(new BoxLayout(linkedPanel, BoxLayout.Y_AXIS));
        linkedPanel.setOpaque(false);
        linkedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblLinked = new JLabel("Thuộc về (*)");
        lblLinked.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinked.setAlignmentX(Component.LEFT_ALIGNMENT);
        linkedPanel.add(lblLinked);
        linkedPanel.add(Box.createVerticalStrut(3));

        cmbLinked = new JComboBox<>();
        cmbLinked.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbLinked.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbLinked.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbLinked.setBackground(Color.WHITE);
        linkedPanel.add(cmbLinked);
        panel.add(linkedPanel);
        panel.add(Box.createVerticalStrut(10));

        // Hint đổi mật khẩu
        JLabel hint = new JLabel("<html><i style='color:gray;font-size:10px'>"
                + "Chọn tài khoản ở bảng → nhập mật khẩu mới<br>"
                + "→ bấm 🔑 Đổi Mật Khẩu</i></html>");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(hint);
        panel.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));

        // Buttons
        JPanel btnPnl = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPnl.setOpaque(false);
        btnPnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        btnPnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAdd = new JButton("➕ Tạo Tài Khoản");
        btnChangePwd = new JButton("🔑 Đổi Mật Khẩu");
        btnToggleLock = new JButton("🔒 Khoá / Mở");
        btnClear = new JButton("🔄 Xóa form");

        styleBtn(btnAdd, new Color(34, 139, 34));
        styleBtn(btnChangePwd, new Color(70, 130, 180));
        styleBtn(btnToggleLock, new Color(185, 45, 45));
        styleBtn(btnClear, new Color(100, 110, 125));

        btnPnl.add(btnAdd);
        btnPnl.add(btnChangePwd);
        btnPnl.add(btnToggleLock);
        btnPnl.add(btnClear);
        panel.add(btnPnl);

        btnAdd.addActionListener(e -> addAccount());
        btnChangePwd.addActionListener(e -> changePassword());
        btnToggleLock.addActionListener(e -> toggleLock());
        btnClear.addActionListener(e -> clearForm());

        updateLinkedPanel(); // init trạng thái ban đầu
        return panel;
    }

    // ──────────────────────────────────── LINKED PANEL LOGIC
    /**
     * Ẩn/hiện dropdown liên kết và điền danh sách đúng với role được chọn.
     * Admin → ẩn. Teacher/Staff/Student → hiện với danh sách tương ứng.
     */
    private void updateLinkedPanel() {
        UserAccount.UserRole role = (UserAccount.UserRole) cmbRole.getSelectedItem();
        boolean needLink = role != null && role != UserAccount.UserRole.Admin;
        linkedPanel.setVisible(needLink);

        if (!needLink)
            return;
        cmbLinked.removeAllItems();

        if (role == UserAccount.UserRole.Teacher) {
            lblLinked.setText("Thuộc về Giáo Viên (*)");
            if (teachers.isEmpty()) {
                cmbLinked.addItem("— Chưa có giáo viên nào —");
            } else {
                teachers.forEach(t -> cmbLinked.addItem(
                        "[ID=" + t.getTeacherId() + "] " + t.getFullName()));
            }
        } else if (role == UserAccount.UserRole.Staff) {
            lblLinked.setText("Thuộc về Nhân Viên (*)");
            if (staffList.isEmpty()) {
                cmbLinked.addItem("— Chưa có nhân viên nào —");
            } else {
                staffList.forEach(s -> cmbLinked.addItem(
                        "[ID=" + s.getStaffId() + "] " + s.getFullName()));
            }
        } else if (role == UserAccount.UserRole.Student) {
            lblLinked.setText("Thuộc về Học Viên (*)");
            if (students.isEmpty()) {
                cmbLinked.addItem("— Chưa có học viên nào —");
            } else {
                students.forEach(s -> cmbLinked.addItem(
                        "[ID=" + s.getStudentId() + "] " + s.getFullName()));
            }
        }
    }

    /** Trích ID từ chuỗi "[ID=5] Nguyễn Văn A" */
    private Long extractLinkedId() {
        String item = (String) cmbLinked.getSelectedItem();
        if (item == null || !item.startsWith("[ID="))
            return null;
        try {
            return Long.parseLong(item.substring(4, item.indexOf(']')).trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ──────────────────────────────────── DATA
    /** Load danh sách tài khoản — KHÔNG truy cập teacher/staff/student proxy */
    private void loadData() {
        tableModel.setRowCount(0);
        userService.findAllUsers().forEach(a -> tableModel.addRow(new Object[] {
                a.getUserId(),
                a.getUsername(),
                a.getRole() != null ? a.getRole().name() : "—",
                Boolean.TRUE.equals(a.getIsActive()) ? "Đang hoạt động" : "Đã khóa"
        }));
        clearForm();
    }

    private void searchData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        userService.findAllUsers().stream()
                .filter(a -> (a.getUsername() != null && a.getUsername().toLowerCase().contains(kw))
                        || (a.getRole() != null && a.getRole().name().toLowerCase().contains(kw)))
                .forEach(a -> tableModel.addRow(new Object[] {
                        a.getUserId(),
                        a.getUsername(),
                        a.getRole() != null ? a.getRole().name() : "—",
                        Boolean.TRUE.equals(a.getIsActive()) ? "Đang hoạt động" : "Đã khóa"
                }));
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        selectedAccountId = (Long) tableModel.getValueAt(row, 0);
        txtUsername.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        String roleName = String.valueOf(tableModel.getValueAt(row, 2));
        for (UserAccount.UserRole r : UserAccount.UserRole.values()) {
            if (r.name().equals(roleName)) {
                cmbRole.setSelectedItem(r);
                break;
            }
        }
        pwdPassword.setText("");
    }

    // ──────────────────────────────────── ACTIONS
    private void addAccount() {
        String user = txtUsername.getText().trim();
        String pwd = new String(pwdPassword.getPassword()).trim();
        if (user.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập Tên đăng nhập và Mật khẩu!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserAccount.UserRole role = (UserAccount.UserRole) cmbRole.getSelectedItem();

        try {
            if (role == UserAccount.UserRole.Admin) {
                userService.createAdminAccount(user, pwd);

            } else {
                Long linkedId = extractLinkedId();
                if (linkedId == null) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn người liên kết cho vai trò " + role.name() + "!\n"
                                    + "(Thêm hồ sơ vào mục Giáo Viên / Nhân Viên / Học Viên trước)",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (role == UserAccount.UserRole.Teacher)
                    userService.createTeacherAccount(user, pwd, linkedId);
                else if (role == UserAccount.UserRole.Staff)
                    userService.createStaffAccount(user, pwd, linkedId);
                else if (role == UserAccount.UserRole.Student)
                    userService.createStudentAccount(user, pwd, linkedId);
            }

            JOptionPane.showMessageDialog(this, "✅ Tạo tài khoản thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        if (selectedAccountId == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn tài khoản từ bảng!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newPwd = new String(pwdPassword.getPassword()).trim();
        if (newPwd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nhập mật khẩu mới vào ô Mật khẩu rồi bấm nút này!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Đổi mật khẩu cho \"" + txtUsername.getText() + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION)
            return;
        try {
            userService.changePassword(selectedAccountId, newPwd);
            JOptionPane.showMessageDialog(this, "✅ Đổi mật khẩu thành công!");
            pwdPassword.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleLock() {
        if (selectedAccountId == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn tài khoản trong bảng!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String status = String.valueOf(tableModel.getValueAt(table.getSelectedRow(), 3));
            if ("Đang hoạt động".equals(status)) {
                userService.lockAccount(selectedAccountId);
                JOptionPane.showMessageDialog(this, "✅ Đã KHÓA tài khoản!");
            } else {
                userService.unlockAccount(selectedAccountId);
                JOptionPane.showMessageDialog(this, "✅ Đã MỞ KHÓA tài khoản!");
            }
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        selectedAccountId = null;
        txtUsername.setText("");
        pwdPassword.setText("");
        cmbRole.setSelectedIndex(0);
        table.clearSelection();
        updateLinkedPanel();
    }

    // ──────────────────────────────────── HELPERS
    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(Color.WHITE);
        f.setForeground(new Color(30, 30, 30));
        f.setCaretColor(COLOR_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void addLabel(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(3));
    }

    private void styleBtn(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
