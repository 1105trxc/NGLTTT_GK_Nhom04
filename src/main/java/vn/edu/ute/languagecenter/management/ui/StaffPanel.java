package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Staff;
import vn.edu.ute.languagecenter.management.service.StaffService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * StaffPanel - Form CRUD quản lý hồ sơ nhân viên.
 * Package: gui.operation (theo quy chuẩn skill swing-module-builder - Người 2)
 * extends JPanel để nhúng vào MainDashboard thông qua CardLayout.
 */
public class StaffPanel extends JPanel {

    private final StaffService staffService = new StaffService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JTextField txtName, txtPhone, txtEmail;
    private JComboBox<Staff.StaffRole> cmbRole;
    private JComboBox<Staff.ActiveStatus> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private Long selectedStaffId = null;

    private static final Color COLOR_PRIMARY = new Color(30, 78, 128);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    public StaffPanel() {
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

        JLabel lbl = new JLabel("🧑\u200D💼  Quản Lý Nhân Viên");
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

        // Lambda: lọc dữ liệu khi click hoặc Enter
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
        String[] cols = { "ID", "Họ Tên", "Chức Vụ", "Điện Thoại", "Email", "Trạng Thái" };
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

        // Ẩn cột ID
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

        JLabel lbl = new JLabel("Thông Tin Nhân Viên");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(COLOR_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(14));

        txtName = addField(panel, "Họ và tên (*)");
        txtPhone = addField(panel, "Điện thoại");
        txtEmail = addField(panel, "Email");

        addLabel(panel, "Chức vụ");
        cmbRole = new JComboBox<>(Staff.StaffRole.values());
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(10));

        addLabel(panel, "Trạng thái");
        cmbStatus = new JComboBox<>(Staff.ActiveStatus.values());
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

        btnAdd.addActionListener(e -> addStaff());
        btnUpdate.addActionListener(e -> updateStaff());
        btnDelete.addActionListener(e -> deleteStaff());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    // --- Logic ---

    private void loadData() {
        tableModel.setRowCount(0);
        for (Staff s : staffService.getAllStaff()) {
            tableModel.addRow(new Object[] {
                    s.getStaffId(), s.getFullName(), s.getRole(),
                    s.getPhone(), s.getEmail(), s.getStatus()
            });
        }
        clearForm();
    }

    /**
     * Tìm kiếm nhân viên bằng Java Stream Lambda.
     * filter() theo tên hoặc tên chức vụ - không thay đổi list gốc (Immutability)
     */
    private void searchData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Staff> list = staffService.getAllStaff(); // lấy List<Staff> từ DB

        // Stream: filter() -> forEach() - không mutate list gốc
        list.stream()
                .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(kw))
                        || (s.getRole() != null && s.getRole().name().toLowerCase().contains(kw)))
                .forEach(s -> tableModel.addRow(new Object[] {
                        s.getStaffId(), s.getFullName(), s.getRole(),
                        s.getPhone(), s.getEmail(), s.getStatus()
                }));
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        selectedStaffId = (Long) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        cmbRole.setSelectedItem(tableModel.getValueAt(row, 2));
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        cmbStatus.setSelectedItem(tableModel.getValueAt(row, 5));
    }

    private void addStaff() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ và Tên!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Staff s = new Staff();
            s.setFullName(txtName.getText().trim());
            s.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            s.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            s.setRole((Staff.StaffRole) cmbRole.getSelectedItem());
            s.setStatus((Staff.ActiveStatus) cmbStatus.getSelectedItem());
            staffService.addStaff(s);
            JOptionPane.showMessageDialog(this, "✅ Thêm nhân viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStaff() {
        if (selectedStaffId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Staff s = staffService.getStaffById(selectedStaffId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
            s.setFullName(txtName.getText().trim());
            s.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            s.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            s.setRole((Staff.StaffRole) cmbRole.getSelectedItem());
            s.setStatus((Staff.ActiveStatus) cmbStatus.getSelectedItem());
            staffService.updateStaff(s);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật nhân viên thành công!");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStaff() {
        if (selectedStaffId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa nhân viên này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                staffService.deleteStaff(selectedStaffId);
                JOptionPane.showMessageDialog(this, "✅ Xóa nhân viên thành công!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedStaffId = null;
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        cmbRole.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    // --- Helpers ---
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
}
