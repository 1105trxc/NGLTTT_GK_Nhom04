package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.service.EnrollmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentPanel extends JPanel {

    // ── Service (class trực tiếp, không interface) ────────────────────────────
    private final EnrollmentService enrollmentService = new EnrollmentService();

    private JComboBox<Student> cboStudent;
    private JComboBox<Class_>  cboClass;
    private JButton            btnEnroll;
    private JButton            btnDrop;
    private JButton            btnRefresh;
    private JLabel             lblSiSo;
    private JTable             tblEnrollment;
    private DefaultTableModel  tableModel;
    private JTextField         txtSearch;
    private JButton            btnSearch;

    public EnrollmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(240, 248, 255));
        pnl.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
            "Ghi Danh Học Viên", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pnl.add(new JLabel("Học viên:"), g);
        cboStudent = new JComboBox<>();
        cboStudent.setPreferredSize(new Dimension(240, 28));
        g.gridx = 1; g.weightx = 1; pnl.add(cboStudent, g);

        g.gridx = 2; g.weightx = 0; pnl.add(new JLabel("Lớp học:"), g);
        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(240, 28));
        g.gridx = 3; g.weightx = 1; pnl.add(cboClass, g);

        lblSiSo = new JLabel("Sĩ số: --/--");
        lblSiSo.setForeground(new Color(70, 130, 180));
        lblSiSo.setFont(new Font("Arial", Font.BOLD, 12));
        g.gridx = 4; g.weightx = 0; pnl.add(lblSiSo, g);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnEnroll  = makeButton("✅ Ghi Danh",      new Color(46, 139, 87));
        btnDrop    = makeButton("❌ Hủy Ghi Danh",  new Color(178, 34, 34));
        btnRefresh = makeButton("🔄 Làm Mới",       new Color(70, 130, 180));
        btnPanel.add(btnEnroll); btnPanel.add(btnDrop); btnPanel.add(btnRefresh);
        g.gridx = 0; g.gridy = 1; g.gridwidth = 5; g.weightx = 1;
        pnl.add(btnPanel, g);

        cboClass.addActionListener(e -> updateSiSoLabel());
        btnEnroll.addActionListener(e -> handleEnroll());
        btnDrop.addActionListener(e -> handleDrop());
        btnRefresh.addActionListener(e -> loadTable());
        return pnl;
    }

    private JPanel buildCenterPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchBar.setOpaque(false);
        txtSearch = new JTextField(20);
        btnSearch = makeButton("🔍 Tìm", new Color(70, 130, 180));
        searchBar.add(new JLabel("Tìm kiếm:")); searchBar.add(txtSearch); searchBar.add(btnSearch);
        pnl.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Học Viên", "Lớp Học", "Ngày Ghi Danh", "Trạng Thái", "Kết Quả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEnrollment = new JTable(tableModel);
        tblEnrollment.setRowHeight(24);
        tblEnrollment.setFont(new Font("Arial", Font.PLAIN, 12));
        tblEnrollment.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblEnrollment.getTableHeader().setBackground(new Color(100, 149, 237));
        tblEnrollment.getTableHeader().setForeground(Color.WHITE);
        tblEnrollment.setSelectionBackground(new Color(173, 216, 230));
        // Ẩn cột ID
        tblEnrollment.getColumnModel().getColumn(0).setMinWidth(0);
        tblEnrollment.getColumnModel().getColumn(0).setMaxWidth(0);
        pnl.add(new JScrollPane(tblEnrollment), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> filterTable(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> filterTable(txtSearch.getText().trim()));
        return pnl;
    }

    private JPanel buildBottomPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel("Tổng bản ghi: 0");
        lbl.setName("lblTotal");
        pnl.add(lbl);
        return pnl;
    }

    private void handleEnroll() {
        Student student = (Student) cboStudent.getSelectedItem();
        Class_  class_  = (Class_)  cboClass.getSelectedItem();
        if (student == null || class_ == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn Học viên và Lớp học.", "Thiếu thông tin",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            enrollmentService.enroll(student, class_);
            JOptionPane.showMessageDialog(this,
                "Ghi danh thành công!\nHọc viên: " + student.getFullName()
                + "\nLớp: " + class_.getClassName(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            updateSiSoLabel();
            loadTable();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Không thể ghi danh", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDrop() {
        int row = tblEnrollment.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn một dòng để hủy ghi danh.", "Chưa chọn",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xác nhận hủy ghi danh?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                enrollmentService.drop(id);
                loadTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        Class_ selected = (Class_) cboClass.getSelectedItem();
        if (selected == null) { updateTotalLabel(0); return; }
        try {
            List<Enrollment> list = enrollmentService.findByClass(selected);
            list.forEach(e -> tableModel.addRow(new Object[]{
                e.getEnrollmentId(),
                e.getStudent().getFullName(),
                e.getClass_().getClassName(),
                e.getEnrollmentDate(),
                e.getStatus(),
                e.getResult()
            }));
            updateTotalLabel(list.size());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không tải được dữ liệu: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword) {
        if (keyword.isEmpty()) { loadTable(); return; }
        String kw = keyword.toLowerCase();
        tableModel.setRowCount(0);
        Class_ selected = (Class_) cboClass.getSelectedItem();
        if (selected == null) return;
        try {
            enrollmentService.findByClass(selected).stream()
                .filter(e -> e.getStudent().getFullName().toLowerCase().contains(kw)
                          || e.getClass_().getClassName().toLowerCase().contains(kw))
                .forEach(e -> tableModel.addRow(new Object[]{
                    e.getEnrollmentId(),
                    e.getStudent().getFullName(),
                    e.getClass_().getClassName(),
                    e.getEnrollmentDate(),
                    e.getStatus(),
                    e.getResult()
                }));
            updateTotalLabel(tableModel.getRowCount());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSiSoLabel() {
        Class_ c = (Class_) cboClass.getSelectedItem();
        if (c == null) { lblSiSo.setText("Sĩ số: --/--"); return; }
        try {
            long current = enrollmentService.findByClass(c).stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.Enrolled)
                .count();
            lblSiSo.setText("Sĩ số: " + current + " / " + c.getMaxStudent());
            lblSiSo.setForeground(current >= c.getMaxStudent()
                ? new Color(178, 34, 34) : new Color(46, 139, 87));
        } catch (Exception ignored) {}
    }

    private void updateTotalLabel(int total) {
        Component south = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (south instanceof JPanel) {
            for (Component c : ((JPanel) south).getComponents()) {
                if (c instanceof JLabel && "lblTotal".equals(c.getName()))
                    ((JLabel) c).setText("Tổng bản ghi: " + total);
            }
        }
    }

    public void setStudents(List<Student> students) {
        cboStudent.removeAllItems();
        students.forEach(cboStudent::addItem);
    }

    public void setClasses(List<Class_> classes) {
        cboClass.removeAllItems();
        classes.forEach(cboClass::addItem);
        updateSiSoLabel();
    }

    private static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Preview – EnrollmentPanel");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(900, 600); f.add(new EnrollmentPanel());
            f.setLocationRelativeTo(null); f.setVisible(true);
        });
    }
}
