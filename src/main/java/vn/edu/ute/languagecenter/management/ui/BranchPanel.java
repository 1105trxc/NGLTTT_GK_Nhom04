package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaBranchRepository;
import vn.edu.ute.languagecenter.management.model.Branch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * BranchPanel - Form CRUD quản lý Chi Nhánh.
 * Package: gui.operation (theo quy chuẩn skill swing-module-builder - Người 2)
 * extends JPanel để nhúng vào MainDashboard thông qua CardLayout.
 */
public class BranchPanel extends JPanel {

    private final JpaBranchRepository branchDAO = new JpaBranchRepository();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JTextField txtName, txtAddress, txtPhone;
    private JComboBox<Branch.ActiveStatus> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private Long selectedBranchId = null;

    private static final Color COLOR_PRIMARY = new Color(30, 78, 128);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    public BranchPanel() {
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
        JLabel lbl = new JLabel("🏢  Quản Lý Chi Nhánh");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(25, 55, 95));
        panel.add(lbl, BorderLayout.WEST);

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sp.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(new Color(30, 30, 30));
        txtSearch.setCaretColor(COLOR_PRIMARY);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JButton btnSearch = new JButton("🔍 Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleBtn(btnSearch, COLOR_PRIMARY);
        styleBtn(btnRefresh, new Color(80, 120, 80));

        // Vẫn giữ sự kiện cho nút Tìm nếu người dùng có thói quen click
        btnSearch.addActionListener(e -> searchData());

        // --- BẮT ĐẦU: LIVE SEARCH ---
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                doLiveSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                doLiveSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                doLiveSearch();
            }

            private void doLiveSearch() {
                // Dùng invokeLater để UI không bị giật khi gõ phím nhanh
                SwingUtilities.invokeLater(() -> searchData());
            }
        });
        // --- KẾT THÚC: LIVE SEARCH ---

        btnRefresh.addActionListener(e -> {
            txtSearch.setText(""); // Xóa trắng ô tìm kiếm khi làm mới
            loadData();
        });

        sp.add(new JLabel("Tìm: "));
        sp.add(txtSearch);
        sp.add(btnSearch);
        sp.add(btnRefresh);
        panel.add(sp, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane buildCenter() {
        String[] cols = {"ID", "Tên Chi Nhánh", "Địa Chỉ", "Điện Thoại", "Trạng Thái"};
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

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225)),
                new EmptyBorder(20, 20, 20, 20)));
        panel.setPreferredSize(new Dimension(280, 0));
        JLabel ttl = new JLabel("Thông Tin Chi Nhánh");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ttl.setForeground(COLOR_PRIMARY);
        ttl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(ttl);
        panel.add(Box.createVerticalStrut(14));
        txtName = addField(panel, "Tên chi nhánh (*)");
        txtAddress = addField(panel, "Địa chỉ");
        txtPhone = addField(panel, "Điện thoại");
        addLabel(panel, "Trạng thái");
        cmbStatus = new JComboBox<>(Branch.ActiveStatus.values());
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
        btnAdd.addActionListener(e -> addBranch());
        btnUpdate.addActionListener(e -> updateBranch());
        btnDelete.addActionListener(e -> deleteBranch());
        btnClear.addActionListener(e -> clearForm());
        return panel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Branch b : branchDAO.findAll()) {
            tableModel.addRow(new Object[]{
                    b.getBranchId(), b.getBranchName(),
                    b.getAddress(), b.getPhone(), b.getStatus()
            });
        }
        clearForm();
    }

    /**
     * Lọc chi nhánh bằng Java Stream Lambda (filter + forEach).
     */
    private void searchData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Branch> list = branchDAO.findAll();
        list.stream()
                .filter(b -> (b.getBranchName() != null && b.getBranchName().toLowerCase().contains(kw))
                        || (b.getAddress() != null && b.getAddress().toLowerCase().contains(kw))
                        || (b.getPhone() != null && b.getPhone().contains(kw))) // Hỗ trợ thêm tìm theo SĐT
                .forEach(b -> tableModel.addRow(new Object[]{
                        b.getBranchId(), b.getBranchName(),
                        b.getAddress(), b.getPhone(), b.getStatus()
                }));
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        selectedBranchId = (Long) tableModel.getValueAt(row, 0);
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtAddress.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        Object s = tableModel.getValueAt(row, 4);
        if (s instanceof Branch.ActiveStatus)
            cmbStatus.setSelectedItem(s);
    }

    private void addBranch() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên chi nhánh!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Branch b = new Branch();
            b.setBranchName(txtName.getText().trim());
            b.setAddress(txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim());
            b.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            b.setStatus((Branch.ActiveStatus) cmbStatus.getSelectedItem());
            branchDAO.save(b);
            JOptionPane.showMessageDialog(this, "✅ Thêm chi nhánh thành công!");

            txtSearch.setText(""); // Xóa text tìm kiếm
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBranch() {
        if (selectedBranchId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi nhánh!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Branch b = branchDAO.findById(selectedBranchId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh!"));
            b.setBranchName(txtName.getText().trim());
            b.setAddress(txtAddress.getText().trim().isEmpty() ? null : txtAddress.getText().trim());
            b.setPhone(txtPhone.getText().trim().isEmpty() ? null : txtPhone.getText().trim());
            b.setStatus((Branch.ActiveStatus) cmbStatus.getSelectedItem());
            branchDAO.update(b);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật chi nhánh thành công!");

            searchData(); // Cập nhật lại kết quả đang search
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBranch() {
        if (selectedBranchId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi nhánh!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa chi nhánh này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                branchDAO.delete(selectedBranchId);
                JOptionPane.showMessageDialog(this, "✅ Xóa chi nhánh thành công!");

                searchData(); // Cập nhật lại kết quả đang search
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedBranchId = null;
        txtName.setText("");
        txtAddress.setText("");
        txtPhone.setText("");
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private JTextField addField(JPanel p, String label) {
        addLabel(p, label);
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBackground(Color.WHITE);
        f.setForeground(new Color(30, 30, 30));
        f.setCaretColor(new Color(30, 78, 128));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
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
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}