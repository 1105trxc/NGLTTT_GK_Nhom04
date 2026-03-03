package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * StudentPanel - Form CRUD quản lý hồ sơ học viên.
 * Package: gui.operation (theo quy chuẩn skill swing-module-builder - Người 2)
 * extends JPanel để nhúng vào MainDashboard thông qua CardLayout.
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService = new StudentService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JTextField txtName, txtDob, txtPhone, txtEmail, txtAddress;
    private JComboBox<Student.Gender> cmbGender;
    private JComboBox<Student.ActiveStatus> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private Long selectedStudentId = null;

    private static final Color COLOR_PRIMARY = new Color(30, 78, 128);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    public StudentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);
        loadData();
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel lbl = new JLabel("👤  Quản Lý Học Viên");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(25, 55, 95));
        panel.add(lbl, BorderLayout.WEST);
        JPanel sp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sp.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

    private JScrollPane buildCenter() {
        String[] cols = { "ID", "Họ Tên", "Ngày Sinh", "Giới Tính", "Điện Thoại", "Email", "Ngày ĐK", "Trạng Thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
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

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(20, 20, 20, 20)));
        panel.setPreferredSize(new Dimension(280, 0));
        JLabel ttl = new JLabel("Thông Tin Học Viên");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ttl.setForeground(COLOR_PRIMARY);
        ttl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(ttl);
        panel.add(Box.createVerticalStrut(14));
        txtName = addField(panel, "Họ và tên (*)");
        txtDob = addField(panel, "Ngày sinh (dd/MM/yyyy)");
        txtPhone = addField(panel, "Điện thoại");
        txtEmail = addField(panel, "Email");
        txtAddress = addField(panel, "Địa chỉ");
        addLabel(panel, "Giới tính");
        cmbGender = new JComboBox<>(Student.Gender.values());
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbGender);
        panel.add(Box.createVerticalStrut(10));
        addLabel(panel, "Trạng thái");
        cmbStatus = new JComboBox<>(Student.ActiveStatus.values());
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(20));
        JPanel btnPnl = new JPanel(new GridLayout(2, 2, 6, 6));
        btnPnl.setOpaque(false);
        btnPnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        btnPnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdd = new JButton("➕ Thêm");
        btnUpdate = new JButton("✏️ Sửa");
        btnDelete = new JButton("🗑 Xóa");
        btnClear = new JButton("🔄 Xóa form");
        styleBtn(btnAdd, new Color(34, 139, 34));
        styleBtn(btnUpdate, COLOR_PRIMARY);
        styleBtn(btnDelete, new Color(185, 45, 45));
        styleBtn(btnClear, new Color(100, 110, 125));
        btnPnl.add(btnAdd);
        btnPnl.add(btnUpdate);
        btnPnl.add(btnDelete);
        btnPnl.add(btnClear);
        panel.add(btnPnl);
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearForm());
        return panel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Student s : studentService.getAllStudents()) {
            tableModel.addRow(new Object[] {
                    s.getStudentId(), s.getFullName(),
                    s.getDateOfBirth() != null ? s.getDateOfBirth().format(fmt) : "",
                    s.getGender(), s.getPhone(), s.getEmail(),
                    s.getRegistrationDate() != null ? s.getRegistrationDate().format(fmt) : "",
                    s.getStatus()
            });
        }
        clearForm();
    }

    /** Lọc học viên bằng Java Stream Lambda (filter + forEach). */
    private void searchData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Student> list = studentService.getAllStudents();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        list.stream()
                .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(kw))
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(kw)))
                .forEach(s -> tableModel.addRow(new Object[] {
                        s.getStudentId(), s.getFullName(),
                        s.getDateOfBirth() != null ? s.getDateOfBirth().format(fmt) : "",
                        s.getGender(), s.getPhone(), s.getEmail(),
                        s.getRegistrationDate() != null ? s.getRegistrationDate().format(fmt) : "",
                        s.getStatus()
                }));
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        selectedStudentId = (Long) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtDob.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        Object g = tableModel.getValueAt(row, 3);
        if (g instanceof Student.Gender)
            cmbGender.setSelectedItem(g);
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        Object sv = tableModel.getValueAt(row, 7);
        if (sv instanceof Student.ActiveStatus)
            cmbStatus.setSelectedItem(sv);
    }

    private void addStudent() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ và Tên!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Student s = new Student();
            s.setFullName(txtName.getText().trim());
            if (!txtDob.getText().trim().isEmpty())
                s.setDateOfBirth(parseDate(txtDob.getText().trim()));
            s.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            s.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            s.setAddress(txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim());
            s.setGender((Student.Gender) cmbGender.getSelectedItem());
            s.setStatus((Student.ActiveStatus) cmbStatus.getSelectedItem());
            studentService.addStudent(s);
            JOptionPane.showMessageDialog(this, "✅ Thêm học viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học viên!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Student s = studentService.getStudentById(selectedStudentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên!"));
            s.setFullName(txtName.getText().trim());
            if (!txtDob.getText().trim().isEmpty())
                s.setDateOfBirth(LocalDate.parse(txtDob.getText().trim()));
            s.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            s.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            s.setAddress(txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim());
            s.setGender((Student.Gender) cmbGender.getSelectedItem());
            s.setStatus((Student.ActiveStatus) cmbStatus.getSelectedItem());
            studentService.updateStudent(s);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật học viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học viên!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa học viên này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(selectedStudentId);
                JOptionPane.showMessageDialog(this, "✅ Xóa học viên thành công!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedStudentId = null;
        txtName.setText("");
        txtDob.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        cmbGender.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private JTextField addField(JPanel p, String label) {
        addLabel(p, label);
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(f);
        p.add(Box.createVerticalStrut(10));
        return f;
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
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Chấp nhận cả dd/MM/yyyy lẫn yyyy-MM-dd */
    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                        "Ngày không hợp lệ '" + s + "'. Dùng dd/MM/yyyy hoặc yyyy-MM-dd");
            }
        }
    }
}
