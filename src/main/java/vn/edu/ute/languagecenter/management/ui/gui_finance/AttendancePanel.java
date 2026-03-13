package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Attendance;
import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.service.AttendanceService;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.EnrollmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AttendancePanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final AttendanceService attendanceService = new AttendanceService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final ClassService classService = new ClassService();

    // ── Components thay thế ComboBox ──────────────────────────────────────────
    private Class_ selectedClass = null;
    private JTextField txtClassName;
    private JButton btnSelectClass;

    private JSpinner spnDate;
    private JButton btnLoad;
    private JButton btnSave;
    private JButton btnExport; // Nút xuất Excel mới thêm
    private JLabel lblSummary;
    private JTable tblAttendance;
    private DefaultTableModel tableModel;
    private Long currentTeacherId;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_STATUS = 2;
    private static final int COL_NOTE = 3;

    public AttendancePanel(Long teacherId) {
        this.currentTeacherId = teacherId;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(240, 255, 240));
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 139, 87), 1),
                "Điểm Danh", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(0, 100, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0;
        pnl.add(new JLabel("Lớp học:"), g);

        // --- Ô hiển thị tên lớp và Nút chọn lớp ---
        txtClassName = new JTextField("Chưa chọn lớp học...");
        txtClassName.setEditable(false);
        txtClassName.setBackground(new Color(245, 245, 245));

        btnSelectClass = new JButton("🔍");
        btnSelectClass.setToolTipText("Mở danh sách tìm kiếm lớp học");

        JPanel pnlClassSelect = new JPanel(new BorderLayout(5, 0));
        pnlClassSelect.setOpaque(false);
        pnlClassSelect.setPreferredSize(new Dimension(250, 28));
        pnlClassSelect.add(txtClassName, BorderLayout.CENTER);
        pnlClassSelect.add(btnSelectClass, BorderLayout.EAST);

        g.gridx = 1;
        g.weightx = 1;
        pnl.add(pnlClassSelect, g);

        g.gridx = 2;
        g.weightx = 0;
        pnl.add(new JLabel("Ngày:"), g);
        SpinnerDateModel dm = new SpinnerDateModel();
        spnDate = new JSpinner(dm);
        spnDate.setEditor(new JSpinner.DateEditor(spnDate, "dd/MM/yyyy"));
        spnDate.setPreferredSize(new Dimension(130, 28));
        g.gridx = 3;
        pnl.add(spnDate, g);

        btnLoad = makeButton("📋 Load Danh Sách", new Color(70, 130, 180));
        g.gridx = 4;
        pnl.add(btnLoad, g);

        btnSave = makeButton("💾 Lưu Điểm Danh", new Color(46, 139, 87));
        g.gridx = 5;
        pnl.add(btnSave, g);

        JButton btnRefresh = makeButton("🔄 Làm Mới", new Color(128, 128, 128));
        g.gridx = 6;
        pnl.add(btnRefresh, g);

        // --- NÚT XUẤT EXCEL ---
        btnExport = makeButton("📗 Xuất Excel", new Color(33, 115, 70));
        g.gridx = 7;
        pnl.add(btnExport, g);

        lblSummary = new JLabel("Có mặt: --  Vắng: --  Trễ: --");
        lblSummary.setFont(new Font("Arial", Font.BOLD, 12));
        lblSummary.setForeground(new Color(25, 25, 112));
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 8; // Đã cập nhật width để bao trọn cả nút Excel
        g.weightx = 1;
        pnl.add(lblSummary, g);

        // --- SỰ KIỆN CHỌN LỚP HỌC ---
        btnSelectClass.addActionListener(e -> {
            try {
                List<Class_> classes;
                // Nếu có ID giáo viên, lọc lớp theo giáo viên đó
                if (currentTeacherId != null) {
                    classes = classService.findByTeacherId(currentTeacherId);
                } else {
                    classes = classService.findAll();
                }

                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                ClassSelectionDialog dialog = new ClassSelectionDialog(parentWindow, classes);
                dialog.setVisible(true);

                Class_ c = dialog.getSelectedClass();
                if (c != null) {
                    selectedClass = c;
                    txtClassName.setText(c.getClassName());

                    // Tự động load danh sách điểm danh ngay khi chọn xong lớp
                    loadAttendanceTable();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải lớp học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLoad.addActionListener(e -> loadAttendanceTable());
        btnSave.addActionListener(e -> saveAttendance());

        btnRefresh.addActionListener(e -> {
            selectedClass = null;
            txtClassName.setText("Chưa chọn lớp học...");
            tableModel.setRowCount(0);
            updateSummary();
        });

        // --- SỰ KIỆN XUẤT EXCEL ---
        btnExport.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lấy tên lớp để đặt tên file mặc định
            String defaultFileName = "DiemDanh_" +
                    (selectedClass != null ? selectedClass.getClassName().replaceAll("[^a-zA-Z0-9]", "_") : "Lop") +
                    "_" + getSelectedDate().toString();

            vn.edu.ute.languagecenter.management.util.ExcelExporter.exportJTableToExcel(
                    tblAttendance, defaultFileName, "Điểm Danh"
            );
        });

        return pnl;
    }

    private JPanel buildTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String[] cols = {"student_id", "Họ Tên Học Viên", "Trạng Thái", "Ghi Chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == COL_STATUS || c == COL_NOTE;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == COL_STATUS ? Attendance.AttendanceStatus.class : String.class;
            }
        };
        tblAttendance = new JTable(tableModel);
        tblAttendance.setRowHeight(28);
        tblAttendance.setFont(new Font("Arial", Font.PLAIN, 12));
        tblAttendance.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblAttendance.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(new Color(46, 139, 87));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(30, 100, 60)));
                setOpaque(true);
                return this;
            }
        });

        tblAttendance.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        tblAttendance.getColumnModel().getColumn(COL_ID).setMaxWidth(0);

        JComboBox<Attendance.AttendanceStatus> statusCombo = new JComboBox<>(Attendance.AttendanceStatus.values());
        tblAttendance.getColumnModel().getColumn(COL_STATUS)
                .setCellEditor(new DefaultCellEditor(statusCombo));

        tblAttendance.getColumnModel().getColumn(COL_STATUS)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object v,
                                                                   boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                        if (v instanceof Attendance.AttendanceStatus) {
                            switch ((Attendance.AttendanceStatus) v) {
                                case Present -> setBackground(new Color(198, 239, 206));
                                case Absent -> setBackground(new Color(255, 199, 206));
                                case Late -> setBackground(new Color(255, 235, 156));
                            }
                            setForeground(Color.BLACK);
                        }
                        return this;
                    }
                });

        tableModel.addTableModelListener(e -> updateSummary());
        pnl.add(new JScrollPane(tblAttendance), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildSouthPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnl.setOpaque(false);
        JButton btnAllPresent = makeButton("✅ Tất Cả Có Mặt", new Color(46, 139, 87));
        JButton btnAllAbsent = makeButton("❌ Tất Cả Vắng", new Color(178, 34, 34));
        btnAllPresent.addActionListener(e -> setAllStatus(Attendance.AttendanceStatus.Present));
        btnAllAbsent.addActionListener(e -> setAllStatus(Attendance.AttendanceStatus.Absent));
        pnl.add(btnAllPresent);
        pnl.add(btnAllAbsent);
        return pnl;
    }

    private void loadAttendanceTable() {
        Class_ cls = selectedClass;
        if (cls == null) {
            JOptionPane.showMessageDialog(this, "Chọn lớp học trước.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate date = getSelectedDate();
        tableModel.setRowCount(0);
        try {
            List<Student> students = enrollmentService.findByClass(cls).stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.Enrolled)
                    .map(Enrollment::getStudent)
                    .sorted(Comparator.comparing(Student::getFullName))
                    .collect(Collectors.toList());

            Map<Long, Attendance> existing = attendanceService.getSession(cls, date).stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getStudentId(), a -> a));

            students.forEach(s -> {
                Attendance att = existing.get(s.getStudentId());
                tableModel.addRow(new Object[]{
                        s.getStudentId(),
                        s.getFullName(),
                        att != null ? att.getStatus() : Attendance.AttendanceStatus.Present,
                        att != null ? att.getNote() : ""
                });
            });
            updateSummary();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi load danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAttendance() {
        Class_ cls = selectedClass;
        if (cls == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để lưu.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (tblAttendance.isEditing())
            tblAttendance.getCellEditor().stopCellEditing();

        LocalDate date = getSelectedDate();
        Map<Student, Attendance.AttendanceStatus> statusMap = new LinkedHashMap<>();
        Map<Student, String> noteMap = new LinkedHashMap<>();

        // Lấy danh sách Student THẬT từ database và đưa vào Map để tra cứu theo ID
        Map<Long, Student> realStudentsMap = enrollmentService.findByClass(cls).stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toMap(Student::getStudentId, s -> s));

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Long studentId = (Long) tableModel.getValueAt(i, COL_ID);

            Student realStudent = realStudentsMap.get(studentId);

            if (realStudent != null) {
                Object stObj = tableModel.getValueAt(i, COL_STATUS);
                Attendance.AttendanceStatus st = (stObj instanceof Attendance.AttendanceStatus)
                        ? (Attendance.AttendanceStatus) stObj
                        : Attendance.AttendanceStatus.Present;
                statusMap.put(realStudent, st);
                noteMap.put(realStudent, (String) tableModel.getValueAt(i, COL_NOTE));
            }
        }

        try {
            attendanceService.saveSession(cls, date, statusMap, noteMap);
            JOptionPane.showMessageDialog(this,
                    "Lưu điểm danh thành công!\n" + lblSummary.getText(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace(); // Bật in lỗi ra console để dễ debug nếu có
            JOptionPane.showMessageDialog(this,
                    "Lỗi lưu điểm danh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setAllStatus(Attendance.AttendanceStatus status) {
        for (int i = 0; i < tableModel.getRowCount(); i++)
            tableModel.setValueAt(status, i, COL_STATUS);
    }

    private void updateSummary() {
        long present = 0, absent = 0, late = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object v = tableModel.getValueAt(i, COL_STATUS);
            if (v == Attendance.AttendanceStatus.Present)
                present++;
            else if (v == Attendance.AttendanceStatus.Absent)
                absent++;
            else if (v == Attendance.AttendanceStatus.Late)
                late++;
        }
        lblSummary.setText("Có mặt: " + present + "  Vắng: " + absent + "  Trễ: " + late);
    }

    private LocalDate getSelectedDate() {
        java.util.Date d = (java.util.Date) spnDate.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
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
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Preview – AttendancePanel");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(800, 550);
            f.add(new AttendancePanel(1L)); // Truyền ID để test
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}