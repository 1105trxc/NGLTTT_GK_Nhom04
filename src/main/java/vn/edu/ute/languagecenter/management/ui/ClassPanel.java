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

    public ClassPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Lớp học"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên lớp:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(15);
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
        txtMaxStudent = new JTextField(8);
        formPanel.add(txtMaxStudent, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3;
        cboStatus = new JComboBox<>(new String[] { "Planned", "Open", "Ongoing", "Completed", "Cancelled" });
        formPanel.add(cboStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm mới");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                fillForm();
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);
        btnSearch = new JButton("Tìm");
        searchPanel.add(btnSearch);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

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
            loadTable(classService.findAll());
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
            List<Teacher> teachers = em.createQuery("SELECT t FROM Teacher t WHERE t.status = 'Active'", Teacher.class)
                    .getResultList();
            em.close();
            for (Teacher t : teachers)
                cboTeacher.addItem(new TeacherItem(t.getTeacherId(), t.getFullName()));
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

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        txtName.setText((String) tableModel.getValueAt(row, 1));
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
            JOptionPane.showMessageDialog(this, "Thêm lớp thành công!");
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
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
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
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
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
}
