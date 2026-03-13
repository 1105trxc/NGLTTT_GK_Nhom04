package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Result;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.EnrollmentService;
import vn.edu.ute.languagecenter.management.service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResultPanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final ResultService resultService = new ResultService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final ClassService classService = new ClassService();

    // ── Components ────────────────────────────────────────────────────────────
    private Class_ selectedClass = null;
    private JTextField txtClassName;
    private JButton btnSelectClass;

    private JButton btnLoad;
    private JButton btnSaveAll;
    private JButton btnExportStats;
    private JButton btnExportExcel; // Nút xuất Excel mới
    private JTable tblResult;
    private DefaultTableModel tableModel;
    private JTextArea txaStats;
    private TableModelListener gradeAutoCalcListener;

    private final UserAccount currentUser;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_SCORE = 2;
    private static final int COL_GRADE = 3;
    private static final int COL_COMMENT = 4;

    public ResultPanel(UserAccount currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        // Chỉ hiển thị Thống Kê cho Admin, Staff, Teacher - không hiển thị cho Student
        if (currentUser == null || currentUser.getRole() != UserAccount.UserRole.Student) {
            add(buildStatsPanel(), BorderLayout.EAST);
        }
    }

    private JPanel buildTopPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(245, 240, 255));
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(106, 90, 205), 1),
                "Nhập Điểm Cuối Kỳ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(75, 0, 130)));

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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnLoad = makeButton("📋 Tải Danh Sách", new Color(70, 130, 180));
        btnSaveAll = makeButton("💾 Lưu Tất Cả Điểm", new Color(46, 139, 87));
        btnExportStats = makeButton("📊 Xem Thống Kê", new Color(106, 90, 205));
        JButton btnRefresh = makeButton("🔄 Làm Mới", new Color(128, 128, 128));

        // --- THÊM NÚT XUẤT EXCEL ---
        btnExportExcel = makeButton("📗 Xuất Excel", new Color(33, 115, 70));

        btnPanel.add(btnLoad);
        btnPanel.add(btnSaveAll);
        btnPanel.add(btnExportStats);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnExportExcel); // Add nút vào panel

        // Ẩn nút lưu và xem thống kê nếu là sinh viên (Sinh viên vẫn được quyền xuất file Excel xem điểm)
        if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student) {
            btnSaveAll.setVisible(false);
            btnExportStats.setVisible(false);
        }

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 3;
        g.weightx = 1;
        pnl.add(btnPanel, g);

        // --- SỰ KIỆN NÚT CHỌN LỚP ---
        btnSelectClass.addActionListener(e -> {
            try {
                List<Class_> classes;
                // Lọc lớp theo Role
                if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Teacher && currentUser.getTeacher() != null) {
                    classes = classService.findByTeacherId(currentUser.getTeacher().getTeacherId());
                } else if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student && currentUser.getStudent() != null) {
                    classes = enrollmentService.findByStudent(currentUser.getStudent()).stream()
                            .map(Enrollment::getClass_)
                            .collect(Collectors.toList());
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

                    // Tự động load danh sách học viên và reset thống kê khi chọn lớp mới
                    loadResultTable();
                    if (txaStats != null) txaStats.setText("(Nhấn 'Xem Thống Kê'\nsau khi tải điểm)");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải lớp học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLoad.addActionListener(e -> loadResultTable());
        btnSaveAll.addActionListener(e -> saveAllResults());
        btnExportStats.addActionListener(e -> refreshStats());
        btnRefresh.addActionListener(e -> {
            selectedClass = null;
            txtClassName.setText("Chưa chọn lớp học...");
            tableModel.setRowCount(0);
            if (txaStats != null) txaStats.setText("(Nhấn 'Xem Thống Kê'\nsau khi tải điểm)");
        });

        // --- SỰ KIỆN XUẤT EXCEL ---
        btnExportExcel.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String defaultFileName = "BangDiem";
            if (selectedClass != null && selectedClass.getClassName() != null) {
                defaultFileName += "_" + selectedClass.getClassName().replaceAll("[^a-zA-Z0-9]", "_");
            }

            vn.edu.ute.languagecenter.management.util.ExcelExporter.exportJTableToExcel(
                    tblResult, defaultFileName, "Bảng Điểm"
            );
        });

        return pnl;
    }

    private JPanel buildTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String[] cols = {"student_id", "Họ Tên Học Viên", "Điểm Số (0-100)", "Xếp Loại", "Nhận Xét"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Khóa không cho sinh viên sửa điểm
                if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student) {
                    return false;
                }
                return c == COL_SCORE || c == COL_COMMENT;
            }
        };

        tblResult = new JTable(tableModel);
        tblResult.setRowHeight(26);
        tblResult.setFont(new Font("Arial", Font.PLAIN, 12));
        tblResult.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblResult.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(new Color(106, 90, 205));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(75, 60, 160)));
                setOpaque(true);
                return this;
            }
        });
        tblResult.setSelectionBackground(new Color(230, 220, 255));

        tblResult.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        tblResult.getColumnModel().getColumn(COL_ID).setMaxWidth(0);

        // Renderer: tô màu cột Grade
        tblResult.getColumnModel().getColumn(COL_GRADE)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object v,
                                                                   boolean sel, boolean foc, int r, int c) {
                        super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                        if (v != null) {
                            String gr = v.toString();
                            if (gr.startsWith("A"))
                                setBackground(new Color(198, 239, 206));
                            else if (gr.startsWith("B"))
                                setBackground(new Color(220, 230, 255));
                            else if (gr.startsWith("C"))
                                setBackground(new Color(255, 235, 156));
                            else if (gr.equals("D"))
                                setBackground(new Color(255, 204, 128));
                            else if (gr.equals("F"))
                                setBackground(new Color(255, 199, 206));
                            else
                                setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                            setHorizontalAlignment(CENTER);
                        }
                        return this;
                    }
                });

        // Listener tự tính grade khi nhập điểm
        gradeAutoCalcListener = new TableModelListener() {
            private boolean updating = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (updating || e.getColumn() != COL_SCORE)
                    return;
                int row = e.getFirstRow();
                if (row < 0)
                    return;
                Object val = tableModel.getValueAt(row, COL_SCORE);
                if (val == null || val.toString().isBlank())
                    return;
                try {
                    String grade = resultService.calculateGrade(new BigDecimal(val.toString()));
                    updating = true;
                    tableModel.setValueAt(grade, row, COL_GRADE);
                    updating = false;
                } catch (NumberFormatException ignored) {
                    updating = true;
                    tableModel.setValueAt("N/A", row, COL_GRADE);
                    updating = false;
                }
            }
        };
        tableModel.addTableModelListener(gradeAutoCalcListener);

        pnl.add(new JScrollPane(tblResult), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildStatsPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setPreferredSize(new Dimension(220, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(106, 90, 205), 1),
                "Thống Kê Lớp", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12), new Color(75, 0, 130)));

        txaStats = new JTextArea("(Nhấn 'Xem Thống Kê'\nsau khi tải điểm)");
        txaStats.setEditable(false);
        txaStats.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txaStats.setBackground(new Color(250, 248, 255));
        txaStats.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnl.add(new JScrollPane(txaStats), BorderLayout.CENTER);
        return pnl;
    }

    // ── Business logic ────────────────────────────────────────────────────────
    private void loadResultTable() {
        Class_ cls = selectedClass;
        if (cls == null) {
            JOptionPane.showMessageDialog(this, "Chọn lớp học trước.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Student> students = enrollmentService.findByClass(cls).stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.Enrolled
                            || e.getStatus() == Enrollment.EnrollmentStatus.Completed)
                    .map(Enrollment::getStudent)
                    .filter(s -> {
                        // BỘ LỌC SINH VIÊN CHỈ ĐƯỢC XEM ĐIỂM CỦA MÌNH
                        if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student
                                && currentUser.getStudent() != null) {
                            return s.getStudentId().equals(currentUser.getStudent().getStudentId());
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(Student::getFullName))
                    .collect(Collectors.toList());

            Map<Long, Result> existingResults = resultService.findByClass(cls).stream()
                    .collect(Collectors.toMap(
                            r -> r.getStudent().getStudentId(), r -> r));

            students.forEach(s -> {
                Result r = existingResults.get(s.getStudentId());
                tableModel.addRow(new Object[]{
                        s.getStudentId(), s.getFullName(),
                        r != null ? r.getScore() : "",
                        r != null ? r.getGrade() : "",
                        r != null ? r.getComment() : ""
                });
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAllResults() {
        Class_ cls = selectedClass;
        if (cls == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để lưu.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (tblResult.isEditing())
            tblResult.getCellEditor().stopCellEditing();

        Map<Student, BigDecimal> scores = new LinkedHashMap<>();
        Map<Student, String> comments = new LinkedHashMap<>();

        // Lấy danh sách Student THẬT từ database để map vào điểm số
        Map<Long, Student> realStudentsMap = enrollmentService.findByClass(cls).stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toMap(Student::getStudentId, s -> s));

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object scoreVal = tableModel.getValueAt(i, COL_SCORE);
            if (scoreVal == null || scoreVal.toString().isBlank())
                continue;

            Long studentId = (Long) tableModel.getValueAt(i, COL_ID);
            Student realStudent = realStudentsMap.get(studentId);

            if (realStudent != null) {
                try {
                    BigDecimal score = new BigDecimal(scoreVal.toString());
                    scores.put(realStudent, score);
                    comments.put(realStudent, Objects.toString(tableModel.getValueAt(i, COL_COMMENT), ""));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Điểm không hợp lệ tại dòng " + (i + 1) + ": " + scoreVal,
                            "Dữ liệu sai", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        try {
            List<Result> saved = resultService.saveResultsForClass(cls, scores, comments);
            JOptionPane.showMessageDialog(this,
                    "Đã lưu điểm cho " + saved.size() + " học viên.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadResultTable();
            if (txaStats != null) refreshStats();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi lưu điểm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStats() {
        Class_ cls = selectedClass;
        if (cls == null || txaStats == null)
            return;
        try {
            Map<String, Long> gradeCount = resultService.countByGrade(cls);
            OptionalDouble avg = resultService.averageScore(cls);

            StringBuilder sb = new StringBuilder();
            sb.append("📊 THỐNG KÊ LỚP\n─────────────────\n");
            List.of("A+", "A", "B+", "B", "C+", "C", "D", "F", "N/A").forEach(gr -> {
                long count = gradeCount.getOrDefault(gr, 0L);
                if (count > 0)
                    sb.append(String.format("%-4s : %d học viên\n", gr, count));
            });
            long total = gradeCount.values().stream().mapToLong(Long::longValue).sum();
            sb.append("─────────────────\n");
            sb.append(String.format("Tổng  : %d\n", total));
            if (avg.isPresent())
                sb.append(String.format("Avg   : %.2f\n", avg.getAsDouble()));
            txaStats.setText(sb.toString());
        } catch (Exception ex) {
            txaStats.setText("Lỗi: " + ex.getMessage());
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
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }
}