package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Course;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.model.Teacher;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.CourseService;
import vn.edu.ute.languagecenter.management.service.RoomService;

import com.toedter.calendar.JDateChooser;
import jakarta.persistence.EntityManager;
import vn.edu.ute.languagecenter.management.db.Jpa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ClassPanel extends JPanel {

    private final ClassService classService = new ClassService();
    private final CourseService courseService = new CourseService();
    private final RoomService roomService = new RoomService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtMaxStudent, txtSearch;
    private JDateChooser dcStartDate, dcEndDate;
    private JComboBox<CourseItem> cboCourse;
    private JComboBox<TeacherItem> cboTeacher;
    private JComboBox<RoomItem> cboRoom;
    private JComboBox<String> cboStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
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
        cboCourse = new JComboBox<>();
        formPanel.add(cboCourse, gbc);

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Giáo viên:"), gbc);
        gbc.gridx = 1;
        cboTeacher = new JComboBox<>();
        formPanel.add(cboTeacher, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Phòng học:"), gbc);
        gbc.gridx = 3;
        cboRoom = new JComboBox<>();
        formPanel.add(cboRoom, gbc);

        // Row 2: Date pickers
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
        cboStatus = new JComboBox<>(new String[] { "Planned", "Open", "Ongoing", "Completed", "Cancelled" });
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
        String[] cols = { "ID", "Tên lớp", "Khóa học", "Giáo viên", "Phòng", "Bắt đầu", "Kết thúc", "Sĩ số max",
                "Trạng thái" };
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
            if (!e.getValueIsAdjusting())
                fillForm();
        });
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = createTextField(20);
        searchPanel.add(txtSearch);
        btnSearch = makeButton("🔍 Tìm", new Color(70, 130, 180));
        searchPanel.add(btnSearch);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel đếm Số lượng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JLabel lblTotal = new JLabel("Tổng bản ghi: 0");
        lblTotal.setName("lblTotal");
        bottomPanel.add(lblTotal);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        btnAdd.addActionListener(e -> addClass());
        btnUpdate.addActionListener(e -> updateClass());
        btnDelete.addActionListener(e -> deleteClass());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchClass());
    }

    public void refreshData() {
        loadCombos();
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

    private void loadCombos() {
        cboCourse.removeAllItems();
        try {
            for (Course c : courseService.findAllActive())
                cboCourse.addItem(new CourseItem(c.getCourseId(), c.getCourseName()));
        } catch (Exception ignored) {
        }

        cboTeacher.removeAllItems();
        cboTeacher.addItem(new TeacherItem(null, "-- Không chọn --"));
        try {
            EntityManager em = Jpa.em();
            List<Teacher> teachers = em.createQuery("SELECT t FROM Teacher t", Teacher.class).getResultList();
            em.close();
            for (Teacher t : teachers) {
                if (t.getStatus() != null && t.getStatus().name().equals("Active")) {
                    cboTeacher.addItem(new TeacherItem(t.getTeacherId(), t.getFullName()));
                }
            }
        } catch (Exception ignored) {
        }

        cboRoom.removeAllItems();
        cboRoom.addItem(new RoomItem(null, "-- Không chọn --"));
        try {
            for (Room r : roomService.findAllActive())
                cboRoom.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        } catch (Exception ignored) {
        }
    }

    private void loadTable(List<Class_> classes) {
        tableModel.setRowCount(0);
        for (Class_ c : classes) {
            tableModel.addRow(new Object[] {
                    c.getClassId(), c.getClassName(),
                    c.getCourse() != null ? c.getCourse().getCourseName() : "",
                    c.getTeacher() != null ? c.getTeacher().getFullName() : "",
                    c.getRoom() != null ? c.getRoom().getRoomName() : "",
                    c.getStartDate(), c.getEndDate(), c.getMaxStudent(), c.getStatus().name()
            });
        }
    }

    private void setSelectedComboItem(JComboBox<?> cbo, String name) {
        if (name == null || name.isEmpty()) {
            if (cbo.getItemCount() > 0)
                cbo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Object obj = cbo.getItemAt(i);
            if (obj != null && obj.toString().equals(name)) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        txtName.setText((String) tableModel.getValueAt(row, 1));

        setSelectedComboItem(cboCourse, (String) tableModel.getValueAt(row, 2));
        setSelectedComboItem(cboTeacher, (String) tableModel.getValueAt(row, 3));
        setSelectedComboItem(cboRoom, (String) tableModel.getValueAt(row, 4));

        Object sd = tableModel.getValueAt(row, 5);
        DateUtil.setLocalDate(dcStartDate, sd instanceof LocalDate ? (LocalDate) sd : null);
        Object ed = tableModel.getValueAt(row, 6);
        DateUtil.setLocalDate(dcEndDate, ed instanceof LocalDate ? (LocalDate) ed : null);
        txtMaxStudent.setText(tableModel.getValueAt(row, 7).toString());
        cboStatus.setSelectedItem(tableModel.getValueAt(row, 8));
    }

    private void addClass() {
        try {
            Class_ c = new Class_();
            c.setClassName(txtName.getText().trim());
            CourseItem ci = (CourseItem) cboCourse.getSelectedItem();
            if (ci != null)
                c.setCourse(courseService.findById(ci.id).orElseThrow());
            TeacherItem ti = (TeacherItem) cboTeacher.getSelectedItem();
            if (ti != null && ti.id != null) {
                EntityManager em = Jpa.em();
                c.setTeacher(em.find(Teacher.class, ti.id));
                em.close();
            }
            RoomItem ri = (RoomItem) cboRoom.getSelectedItem();
            if (ri != null && ri.id != null)
                c.setRoom(roomService.findById(ri.id).orElseThrow());
            LocalDate startDate = DateUtil.getLocalDate(dcStartDate);
            if (startDate == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu.");
            c.setStartDate(startDate);
            c.setEndDate(DateUtil.getLocalDate(dcEndDate));
            c.setMaxStudent(Integer.parseInt(txtMaxStudent.getText().trim()));
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
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Class_ c = classService.findById(id).orElseThrow();
            c.setClassName(txtName.getText().trim());
            CourseItem ci = (CourseItem) cboCourse.getSelectedItem();
            if (ci != null)
                c.setCourse(courseService.findById(ci.id).orElseThrow());
            TeacherItem ti = (TeacherItem) cboTeacher.getSelectedItem();
            if (ti != null && ti.id != null) {
                EntityManager em = Jpa.em();
                c.setTeacher(em.find(Teacher.class, ti.id));
                em.close();
            } else {
                c.setTeacher(null);
            }
            RoomItem ri = (RoomItem) cboRoom.getSelectedItem();
            if (ri != null && ri.id != null)
                c.setRoom(roomService.findById(ri.id).orElseThrow());
            else
                c.setRoom(null);
            LocalDate startDate = DateUtil.getLocalDate(dcStartDate);
            if (startDate == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu.");
            c.setStartDate(startDate);
            c.setEndDate(DateUtil.getLocalDate(dcEndDate));
            c.setMaxStudent(Integer.parseInt(txtMaxStudent.getText().trim()));
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
        table.clearSelection();
    }

    private record CourseItem(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record TeacherItem(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record RoomItem(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
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
