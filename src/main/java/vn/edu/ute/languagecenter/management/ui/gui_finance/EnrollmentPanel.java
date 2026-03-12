package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaClassRepository;
import vn.edu.ute.languagecenter.management.service.EnrollmentService;
import vn.edu.ute.languagecenter.management.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EnrollmentPanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final StudentService studentService = new StudentService();
    // ClassService chưa có → dùng JpaClassRepository trực tiếp
    private final JpaClassRepository classRepo = new JpaClassRepository();

    private Student selectedStudent = null; // Lưu học viên đang được chọn
    private JTextField txtStudentName;      // Hiển thị tên
    private JButton btnSelectStudent;       // Nút bấm mở cửa sổ
    private Class_ selectedClass = null; // Lưu lớp đang chọn
    private JTextField txtClassName;     // Hiển thị tên lớp
    private JButton btnSelectClass;      // Nút mở cửa sổ
    private JButton btnEnroll;
    private JButton btnDrop;
    private JButton btnRefresh;
    private JLabel lblSiSo;
    private JTable tblEnrollment;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnSearch;

    public EnrollmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        add(buildTopPanel(), BorderLayout.NORTH);
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
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0;
        pnl.add(new JLabel("Học viên:"), g);
        txtStudentName = new JTextField("Chưa chọn học viên...");
        txtStudentName.setEditable(false);
        txtStudentName.setBackground(new Color(245, 245, 245));

        btnSelectStudent = new JButton("🔍");
        btnSelectStudent.setToolTipText("Mở danh sách tìm kiếm học viên");

        JPanel pnlStudentSelect = new JPanel(new BorderLayout(5, 0));
        pnlStudentSelect.setOpaque(false);
        pnlStudentSelect.setPreferredSize(new Dimension(240, 28));
        pnlStudentSelect.add(txtStudentName, BorderLayout.CENTER);
        pnlStudentSelect.add(btnSelectStudent, BorderLayout.EAST);

        g.gridx = 1;
        g.weightx = 1;
        pnl.add(pnlStudentSelect, g);

        g.gridx = 2;
        g.weightx = 0;
        pnl.add(new JLabel("Lớp học:"), g);
        txtClassName = new JTextField("Chưa chọn lớp học...");
        txtClassName.setEditable(false);
        txtClassName.setBackground(new Color(245, 245, 245));

        btnSelectClass = new JButton("🔍");
        btnSelectClass.setToolTipText("Mở danh sách tìm kiếm lớp học");

        JPanel pnlClassSelect = new JPanel(new BorderLayout(5, 0));
        pnlClassSelect.setOpaque(false);
        pnlClassSelect.setPreferredSize(new Dimension(240, 28));
        pnlClassSelect.add(txtClassName, BorderLayout.CENTER);
        pnlClassSelect.add(btnSelectClass, BorderLayout.EAST);

        g.gridx = 3;
        g.weightx = 1;
        pnl.add(pnlClassSelect, g);

        lblSiSo = new JLabel("Sĩ số: --/--");
        lblSiSo.setForeground(new Color(70, 130, 180));
        lblSiSo.setFont(new Font("Arial", Font.BOLD, 12));
        g.gridx = 4;
        g.weightx = 0;
        pnl.add(lblSiSo, g);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnEnroll = makeButton("✅ Ghi Danh", new Color(46, 139, 87));
        btnDrop = makeButton("❌ Hủy Ghi Danh", new Color(178, 34, 34));
        btnRefresh = makeButton("🔄 Làm Mới", new Color(70, 130, 180));
        btnPanel.add(btnEnroll);
        btnPanel.add(btnDrop);
        btnPanel.add(btnRefresh);
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 5;
        g.weightx = 1;
        pnl.add(btnPanel, g);

        btnSelectStudent.addActionListener(e -> {
            try {
                // Load danh sách mới nhất từ DB
                List<Student> activeStudents = studentService.getActiveStudents();

                // Mở cửa sổ chọn (Dialog)
                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                StudentSelectionDialog dialog = new StudentSelectionDialog(parentWindow, activeStudents);
                dialog.setVisible(true); // Code sẽ dừng ở đây chờ người dùng đóng cửa sổ

                // Lấy kết quả trả về
                Student s = dialog.getSelectedStudent();
                if (s != null) {
                    selectedStudent = s;
                    // Hiển thị Tên - SĐT cho rõ ràng
                    txtStudentName.setText(s.getFullName() + " - " + (s.getPhone() != null ? s.getPhone() : ""));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải học viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSelectClass.addActionListener(e -> {
            try {
                // Lấy toàn bộ lớp từ DB (bạn có thể viết thêm hàm getActiveClasses bên ClassService để chỉ lấy lớp đang mở)
                List<Class_> allClasses = classRepo.findAll();

                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                ClassSelectionDialog dialog = new ClassSelectionDialog(parentWindow, allClasses);
                dialog.setVisible(true);

                Class_ c = dialog.getSelectedClass();
                if (c != null) {
                    selectedClass = c;
                    txtClassName.setText(c.getClassName());

                    // Khi chọn lớp xong thì cập nhật sĩ số và load danh sách học viên của lớp đó
                    updateSiSoLabel();
                    loadTable();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải lớp học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEnroll.addActionListener(e -> handleEnroll());
        btnDrop.addActionListener(e -> handleDrop());
        btnRefresh.addActionListener(e -> {
            loadTable();
        });
        return pnl;
    }

    private JPanel buildCenterPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchBar.setOpaque(false);
        txtSearch = new JTextField(20);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(new Color(30, 30, 30));
        txtSearch.setCaretColor(new Color(70, 130, 180));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 190, 215), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        btnSearch = makeButton("🔍 Tìm", new Color(70, 130, 180));
        searchBar.add(new JLabel("Tìm kiếm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        pnl.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Học Viên", "Lớp Học", "Ngày Ghi Danh", "Trạng Thái", "Kết Quả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblEnrollment = new JTable(tableModel);
        tblEnrollment.setRowHeight(24);
        tblEnrollment.setFont(new Font("Arial", Font.PLAIN, 12));
        tblEnrollment.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblEnrollment.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(new Color(100, 149, 237));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(60, 100, 180)));
                setOpaque(true);
                return this;
            }
        });
        tblEnrollment.setSelectionBackground(new Color(173, 216, 230));
        tblEnrollment.getColumnModel().getColumn(0).setMinWidth(0);
        tblEnrollment.getColumnModel().getColumn(0).setMaxWidth(0);
        pnl.add(new JScrollPane(tblEnrollment), BorderLayout.CENTER);

        // Vẫn giữ lại sự kiện click nút Tìm kiếm (phòng hờ người dùng vẫn có thói quen click)
        btnSearch.addActionListener(e -> filterTable(txtSearch.getText().trim()));

        // --- THÊM MỚI: Bắt sự kiện gõ phím (Live Search) ---
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                doSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                doSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                doSearch();
            }

            private void doSearch() {
                // Dùng SwingUtilities.invokeLater để đảm bảo UI không bị giật/lag khi gõ nhanh
                SwingUtilities.invokeLater(() -> filterTable(txtSearch.getText().trim()));
            }
        });

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

        // Dùng biến selectedStudent thay vì cboStudent
        if (selectedStudent == null || selectedClass == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn Học viên và Lớp học.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            enrollmentService.enroll(selectedStudent, selectedClass); // Truyền selectedStudent vào
            JOptionPane.showMessageDialog(this,
                    "Ghi danh thành công!\nHọc viên: " + selectedStudent.getFullName()
                            + "\nLớp: " + selectedClass.getClassName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Xóa rỗng form sau khi ghi danh xong cho tiện
            selectedStudent = null;
            txtStudentName.setText("Chưa chọn học viên...");

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
        if (selectedClass == null) {
            updateTotalLabel(0);
            return;
        }
        try {
            List<Enrollment> list = enrollmentService.findByClass(selectedClass);
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
        if (keyword.isEmpty()) {
            loadTable();
            return;
        }
        String kw = keyword.toLowerCase();
        tableModel.setRowCount(0);
        if (selectedClass == null)
            return;
        try {
            enrollmentService.findByClass(selectedClass).stream()
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
        if (selectedClass == null) {
            lblSiSo.setText("Sĩ số: --/--");
            return;
        }
        try {
            long current = enrollmentService.findByClass(selectedClass).stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.Enrolled)
                    .count();
            lblSiSo.setText("Sĩ số: " + current + " / " + selectedClass.getMaxStudent());
            lblSiSo.setForeground(current >= selectedClass.getMaxStudent()
                    ? new Color(178, 34, 34)
                    : new Color(46, 139, 87));
        } catch (Exception ignored) {
        }
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

    private static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
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
            f.setSize(900, 600);
            f.add(new EnrollmentPanel());
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
