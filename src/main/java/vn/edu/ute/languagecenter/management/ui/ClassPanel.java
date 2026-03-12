package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Course;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.model.Teacher;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.CourseService;
import vn.edu.ute.languagecenter.management.service.RoomService;
import vn.edu.ute.languagecenter.management.service.TeacherService; // <-- Đảm bảo có class này
import vn.edu.ute.languagecenter.management.ui.gui_finance.CourseSelectionDialog;
import vn.edu.ute.languagecenter.management.ui.gui_finance.RoomSelectionDialog;
import vn.edu.ute.languagecenter.management.ui.gui_finance.TeacherSelectionDialog;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ClassPanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();
    private final RoomService roomService = new RoomService();
    private final TeacherService teacherService = new TeacherService();

    // ── Components ────────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtMaxStudent, txtSearch;
    private JDateChooser dcStartDate, dcEndDate;
    private JComboBox<String> cboStatus;

    // Components thay cho ComboBox cũ
    private Course selectedCourse = null;
    private JTextField txtCourseName;
    private JButton btnSelectCourse;

    private Teacher selectedTeacher = null;
    private JTextField txtTeacherName;
    private JButton btnSelectTeacher;

    private Room selectedRoom = null;
    private JTextField txtRoomName;
    private JButton btnSelectRoom;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearchAction;
    private Long teacherId;

    public ClassPanel(Long teacherId) {
        this.teacherId = teacherId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Thông Tin Lớp Học", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên lớp:"), gbc);

        gbc.gridx = 1;
        txtName = createTextField(15);
        formPanel.add(txtName, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Khóa học:"), gbc);

        gbc.gridx = 3;
        txtCourseName = createTextField(15);
        txtCourseName.setEditable(false);
        txtCourseName.setText("Chưa chọn khóa học...");
        btnSelectCourse = new JButton("🔍");
        JPanel pnlCourse = new JPanel(new BorderLayout(5, 0));
        pnlCourse.setOpaque(false);
        pnlCourse.add(txtCourseName, BorderLayout.CENTER);
        pnlCourse.add(btnSelectCourse, BorderLayout.EAST);
        formPanel.add(pnlCourse, gbc);

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Giáo viên:"), gbc);

        gbc.gridx = 1;
        txtTeacherName = createTextField(15);
        txtTeacherName.setEditable(false);
        txtTeacherName.setText("Chưa chọn giáo viên...");
        btnSelectTeacher = new JButton("🔍");
        JPanel pnlTeacher = new JPanel(new BorderLayout(5, 0));
        pnlTeacher.setOpaque(false);
        pnlTeacher.add(txtTeacherName, BorderLayout.CENTER);
        pnlTeacher.add(btnSelectTeacher, BorderLayout.EAST);
        formPanel.add(pnlTeacher, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Phòng học:"), gbc);

        gbc.gridx = 3;
        txtRoomName = createTextField(15);
        txtRoomName.setEditable(false);
        txtRoomName.setText("Chưa chọn phòng...");
        btnSelectRoom = new JButton("🔍");
        JPanel pnlRoom = new JPanel(new BorderLayout(5, 0));
        pnlRoom.setOpaque(false);
        pnlRoom.add(txtRoomName, BorderLayout.CENTER);
        pnlRoom.add(btnSelectRoom, BorderLayout.EAST);
        formPanel.add(pnlRoom, gbc);

        // Row 2
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Ngày bắt đầu:"), gbc);
        gbc.gridx = 1;
        dcStartDate = DateUtil.createDateChooser();
        formPanel.add(dcStartDate, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 3;
        dcEndDate = DateUtil.createDateChooser();
        formPanel.add(dcEndDate, gbc);

        // Row 3
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Sĩ số tối đa:"), gbc);
        gbc.gridx = 1;
        txtMaxStudent = createTextField(8);
        formPanel.add(txtMaxStudent, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3;
        cboStatus = new JComboBox<>(new String[]{"Planned", "Open", "Ongoing", "Completed", "Cancelled"});
        formPanel.add(cboStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnAdd = makeButton("✅ Thêm", new Color(46, 139, 87));
        btnUpdate = makeButton("✏️ Cập nhật", new Color(245, 158, 11));
        btnDelete = makeButton("❌ Xóa", new Color(178, 34, 34));
        btnClear = makeButton("🔄 Làm mới", new Color(70, 130, 180));
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        if (teacherId != null) {
            formPanel.setVisible(false);
        }

        add(formPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Tên lớp", "Khóa học", "Giáo viên", "Phòng", "Bắt đầu", "Kết thúc", "Sĩ số max", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(new Color(100, 149, 237));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(60, 100, 180)));
                setOpaque(true);
                return this;
            }
        });
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = createTextField(20);
        searchPanel.add(txtSearch);
        btnSearchAction = makeButton("🔍 Tìm", new Color(70, 130, 180));
        searchPanel.add(btnSearchAction);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JLabel lblTotal = new JLabel("Tổng bản ghi: 0");
        lblTotal.setName("lblTotal");
        bottomPanel.add(lblTotal);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events Dialog
        btnSelectCourse.addActionListener(e -> openCourseDialog());
        btnSelectTeacher.addActionListener(e -> openTeacherDialog());
        btnSelectRoom.addActionListener(e -> openRoomDialog());

        // Events CRUD
        btnAdd.addActionListener(e -> addClass());
        btnUpdate.addActionListener(e -> updateClass());
        btnDelete.addActionListener(e -> deleteClass());
        btnClear.addActionListener(e -> clearForm());
        btnSearchAction.addActionListener(e -> searchClass());
    }

    private void openCourseDialog() {
        try {
            List<Course> list = courseService.findAllActive();
            Window owner = SwingUtilities.getWindowAncestor(this);
            CourseSelectionDialog dialog = new CourseSelectionDialog(owner, list);
            dialog.setVisible(true);

            Course result = dialog.getSelectedCourse();
            if (result != null) {
                selectedCourse = result;
                txtCourseName.setText(result.getCourseName());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải khóa học: " + ex.getMessage());
        }
    }

    private void openTeacherDialog() {
        try {
            List<Teacher> list = teacherService.getActiveTeachers();
            Window owner = SwingUtilities.getWindowAncestor(this);
            TeacherSelectionDialog dialog = new TeacherSelectionDialog(owner, list);
            dialog.setVisible(true);

            // Xử lý lấy đối tượng ra
            if (!dialog.isVisible()) {
                selectedTeacher = dialog.getSelectedTeacher();
                if (selectedTeacher != null) {
                    txtTeacherName.setText(selectedTeacher.getFullName());
                } else {
                    // Xóa trống
                    txtTeacherName.setText("Chưa chọn giáo viên...");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải giáo viên: " + ex.getMessage());
        }
    }

    private void openRoomDialog() {
        try {
            List<Room> list = roomService.findAllActive();
            Window owner = SwingUtilities.getWindowAncestor(this);
            RoomSelectionDialog dialog = new RoomSelectionDialog(owner, list);
            dialog.setVisible(true);

            Room result = dialog.getSelectedRoom();
            if (result != null) {
                selectedRoom = result;
                txtRoomName.setText(result.getRoomName());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải phòng: " + ex.getMessage());
        }
    }

    public void refreshData() {
        try {
            if (teacherId != null) {
                loadTable(classService.findByTeacherId(teacherId));
            } else {
                loadTable(classService.findAll());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadTable(List<Class_> classes) {
        tableModel.setRowCount(0);
        for (Class_ c : classes) {
            tableModel.addRow(new Object[]{
                    c.getClassId(), c.getClassName(),
                    c.getCourse() != null ? c.getCourse().getCourseName() : "",
                    c.getTeacher() != null ? c.getTeacher().getFullName() : "",
                    c.getRoom() != null ? c.getRoom().getRoomName() : "",
                    c.getStartDate(), c.getEndDate(), c.getMaxStudent(), c.getStatus().name()
            });
        }
        updateTotalLabel(classes.size());
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

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Class_ c = classService.findById(id).orElse(null);

            if (c != null) {
                txtName.setText(c.getClassName());

                // Set Course
                selectedCourse = c.getCourse();
                txtCourseName.setText(selectedCourse != null ? selectedCourse.getCourseName() : "Chưa chọn khóa học...");

                // Set Teacher
                selectedTeacher = c.getTeacher();
                txtTeacherName.setText(selectedTeacher != null ? selectedTeacher.getFullName() : "Chưa chọn giáo viên...");

                // Set Room
                selectedRoom = c.getRoom();
                txtRoomName.setText(selectedRoom != null ? selectedRoom.getRoomName() : "Chưa chọn phòng...");

                DateUtil.setLocalDate(dcStartDate, c.getStartDate());
                DateUtil.setLocalDate(dcEndDate, c.getEndDate());
                txtMaxStudent.setText(c.getMaxStudent() != null ? c.getMaxStudent().toString() : "0");
                cboStatus.setSelectedItem(c.getStatus().name());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addClass() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Class_ c = new Class_();
            c.setClassName(txtName.getText().trim());
            c.setCourse(selectedCourse);
            c.setTeacher(selectedTeacher); // Có thể null
            c.setRoom(selectedRoom);       // Có thể null

            LocalDate startDate = DateUtil.getLocalDate(dcStartDate);
            if (startDate == null) throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu.");

            c.setStartDate(startDate);
            c.setEndDate(DateUtil.getLocalDate(dcEndDate));

            String maxStr = txtMaxStudent.getText().trim();
            c.setMaxStudent(maxStr.isEmpty() ? 0 : Integer.parseInt(maxStr));
            c.setStatus(Class_.ClassStatus.valueOf((String) cboStatus.getSelectedItem()));

            classService.save(c);
            JOptionPane.showMessageDialog(this, "Thêm lớp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClass() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn lớp cần cập nhật.");
            return;
        }
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Class_ c = classService.findById(id).orElseThrow(() -> new Exception("Không tìm thấy lớp."));

            c.setClassName(txtName.getText().trim());
            c.setCourse(selectedCourse);
            c.setTeacher(selectedTeacher);
            c.setRoom(selectedRoom);

            LocalDate startDate = DateUtil.getLocalDate(dcStartDate);
            if (startDate == null) throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu.");
            c.setStartDate(startDate);
            c.setEndDate(DateUtil.getLocalDate(dcEndDate));

            String maxStr = txtMaxStudent.getText().trim();
            c.setMaxStudent(maxStr.isEmpty() ? 0 : Integer.parseInt(maxStr));
            c.setStatus(Class_.ClassStatus.valueOf((String) cboStatus.getSelectedItem()));

            classService.update(c);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClass() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn lớp cần xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            classService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchClass() {
        String kw = txtSearch.getText().trim();
        try {
            loadTable(kw.isEmpty() ? classService.findAll() : classService.findByName(kw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtMaxStudent.setText("");
        txtSearch.setText("");
        dcStartDate.setDate(null);
        dcEndDate.setDate(null);
        cboStatus.setSelectedIndex(0);

        selectedCourse = null;
        txtCourseName.setText("Chưa chọn khóa học...");

        selectedTeacher = null;
        txtTeacherName.setText("Chưa chọn giáo viên...");

        selectedRoom = null;
        txtRoomName.setText("Chưa chọn phòng...");

        table.clearSelection();
    }

    // Tiện ích UI
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

    private JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(30, 30, 30));
        tf.setCaretColor(new Color(70, 130, 180));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 190, 215), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }
}