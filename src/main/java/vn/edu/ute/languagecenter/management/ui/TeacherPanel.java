package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Teacher;
import vn.edu.ute.languagecenter.management.service.TeacherService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * TeacherPanel - Form CRUD quản lý hồ sơ giáo viên.
 * Package: gui.operation (theo quy chuẩn skill swing-module-builder - Người 2)
 * extends JPanel (không phải JFrame) để nhúng vào MainDashboard qua CardLayout.
 *
 * Chức năng:
 * - Hiển thị danh sách giáo viên trong JTable
 * - Thêm / Sửa / Xoá giáo viên
 * - Tìm kiếm / lọc bằng Java Stream Lambda
 */
public class TeacherPanel extends JPanel {

    private final TeacherService teacherService = new TeacherService();

    // Bảng dữ liệu
    private JTable table;
    private DefaultTableModel tableModel;

    // Ô tìm kiếm
    private JTextField txtSearch;

    // Các trường nhập liệu form
    private JTextField txtName, txtPhone, txtEmail, txtSpecialty, txtHireDate;
    private JComboBox<Teacher.ActiveStatus> cmbStatus;

    // Nút bấm
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // ID của giáo viên đang được chọn trong bảng (null = chưa chọn)
    private Long selectedTeacherId = null;

    private static final Color COLOR_PRIMARY = new Color(30, 78, 128);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    public TeacherPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        loadData(); // nạp dữ liệu từ DB khi khởi tạo
    }

    /** Tiêu đề + thanh tìm kiếm */
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lbl = new JLabel("👨\u200D🏫  Quản Lý Giáo Viên");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(25, 55, 95));
        panel.add(lbl, BorderLayout.WEST);

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sp.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setToolTipText("Tìm kiếm theo tên hoặc chuyên môn");

        JButton btnSearch = new JButton("🔍 Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleBtn(btnSearch, COLOR_PRIMARY);
        styleBtn(btnRefresh, new Color(80, 120, 80));

        // Lambda: gọi searchData khi click hoặc Enter
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

    /** JScrollPane chứa JTable danh sách giáo viên */
    private JScrollPane buildCenter() {
        String[] cols = { "ID", "Họ Tên", "Điện Thoại", "Email", "Chuyên Môn", "Ngày Thuê", "Trạng Thái" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            } // chỉ đọc
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 245));
        table.setGridColor(new Color(220, 225, 235));

        // Ẩn cột ID (index 0) để hide khỏi UI nhưng vẫn truy cập được
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Khi chọn dòng: điền dữ liệu vào form bên phải
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillFormFromSelectedRow();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));
        return scroll;
    }

    /** Form nhập liệu bên phải */
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(20, 20, 20, 20)));
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel lblForm = new JLabel("Thông Tin Giáo Viên");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForm.setForeground(COLOR_PRIMARY);
        lblForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblForm);
        panel.add(Box.createVerticalStrut(14));

        // Các trường nhập liệu
        txtName = addFormField(panel, "Họ và tên (*)");
        txtPhone = addFormField(panel, "Điện thoại");
        txtEmail = addFormField(panel, "Email");
        txtSpecialty = addFormField(panel, "Chuyên môn");
        txtHireDate = addFormField(panel, "Ngày thuê (dd/MM/yyyy)");

        addLabel(panel, "Trạng thái");
        cmbStatus = new JComboBox<>(Teacher.ActiveStatus.values());
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

        btnAdd.addActionListener(e -> addTeacher());
        btnUpdate.addActionListener(e -> updateTeacher());
        btnDelete.addActionListener(e -> deleteTeacher());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    // =============================================================
    // LOGIC THAO TÁC DỮ LIỆU
    // =============================================================

    /** Nạp toàn bộ giáo viên từ DB lên JTable */
    private void loadData() {
        tableModel.setRowCount(0); // xóa dữ liệu cũ trong bảng
        List<Teacher> list = teacherService.getAllTeachers();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Teacher t : list) {
            tableModel.addRow(new Object[] {
                    t.getTeacherId(), t.getFullName(), t.getPhone(),
                    t.getEmail(), t.getSpecialty(),
                    t.getHireDate() != null ? t.getHireDate().format(fmt) : "",
                    t.getStatus()
            });
        }
        clearForm();
    }

    /**
     * Lọc giáo viên theo từ khoá tìm kiếm.
     * Dùng Java Stream Lambda (tuân thủ skill lambda-stream-processor):
     * - Nhận List<Teacher> từ DAO
     * - filter() theo tên hoặc chuyên môn
     * - forEach() để đổ dữ liệu lên bảng
     */
    private void searchData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Teacher> list = teacherService.getAllTeachers(); // lấy từ DB (List<T>)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Stream pipeline: filter -> forEach (không thay đổi list gốc - tính
        // Immutability)
        list.stream()
                // Intermediate operation: filter theo tên hoặc chuyên môn
                .filter(t -> (t.getFullName() != null && t.getFullName().toLowerCase().contains(keyword))
                        || (t.getSpecialty() != null && t.getSpecialty().toLowerCase().contains(keyword)))
                // Terminal operation: forEach để thêm từng dòng vào JTable
                .forEach(t -> tableModel.addRow(new Object[] {
                        t.getTeacherId(), t.getFullName(), t.getPhone(),
                        t.getEmail(), t.getSpecialty(),
                        t.getHireDate() != null ? t.getHireDate().format(fmt) : "",
                        t.getStatus()
                }));
    }

    /** Điền dữ liệu của dòng đang chọn trong bảng vào form */
    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        selectedTeacherId = (Long) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        txtSpecialty.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        txtHireDate.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        Object sv = tableModel.getValueAt(row, 6);
        if (sv instanceof Teacher.ActiveStatus)
            cmbStatus.setSelectedItem(sv);
    }

    /** Thêm giáo viên mới từ dữ liệu form */
    private void addTeacher() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ và Tên!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Teacher t = new Teacher();
            t.setFullName(txtName.getText().trim());
            t.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            t.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            t.setSpecialty(txtSpecialty.getText().trim().isEmpty() ? null : txtSpecialty.getText().trim());
            if (!txtHireDate.getText().trim().isEmpty())
                t.setHireDate(parseDate(txtHireDate.getText().trim()));
            t.setStatus((Teacher.ActiveStatus) cmbStatus.getSelectedItem());
            teacherService.addTeacher(t);
            JOptionPane.showMessageDialog(this, "✅ Thêm giáo viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cập nhật thông tin giáo viên đang chọn */
    private void updateTeacher() {
        if (selectedTeacherId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một giáo viên trong bảng!", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Teacher t = teacherService.getTeacherById(selectedTeacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên!"));
            t.setFullName(txtName.getText().trim());
            t.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            t.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            t.setSpecialty(txtSpecialty.getText().trim().isEmpty() ? null : txtSpecialty.getText().trim());
            if (!txtHireDate.getText().trim().isEmpty())
                t.setHireDate(parseDate(txtHireDate.getText().trim()));
            t.setStatus((Teacher.ActiveStatus) cmbStatus.getSelectedItem());
            teacherService.updateTeacher(t);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật giáo viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Xóa giáo viên đang chọn khỏi DB */
    private void deleteTeacher() {
        if (selectedTeacherId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một giáo viên trong bảng!", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa giáo viên này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                teacherService.deleteTeacher(selectedTeacherId);
                JOptionPane.showMessageDialog(this, "✅ Xóa giáo viên thành công!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Reset form nhập liệu về trạng thái rỗng */
    private void clearForm() {
        selectedTeacherId = null;
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtSpecialty.setText("");
        txtHireDate.setText("");
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    // =============================================================
    // HELPERS
    // =============================================================

    private JTextField addFormField(JPanel p, String label) {
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

    /**
     * Phân tích chuỗi ngày, chấp nhận cả 2 định dạng:
     * - dd/MM/yyyy (ví dụ: 25/03/2005) - định dạng hiển thị trong bảng
     * - yyyy-MM-dd (ví dụ: 2005-03-25) - định dạng ISO chuẩn
     */
    private LocalDate parseDate(String s) {
        try {
            // Thử parse ISO trước (yyyy-MM-dd)
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e1) {
            try {
                // Nếu lỗi, thử parse theo định dạng Việt Nam (dd/MM/yyyy)
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                        "Ngày không hợp lệ '" + s + "'. Dùng định dạng dd/MM/yyyy hoặc yyyy-MM-dd");
            }
        }
    }
}
