package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.model.Schedule;
import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.RoomService;
import vn.edu.ute.languagecenter.management.service.ScheduleService;
import vn.edu.ute.languagecenter.management.service.EnrollmentService;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulePanel extends JPanel {

    private final ScheduleService scheduleService = new ScheduleService();
    private final ClassService classService = new ClassService();
    private final RoomService roomService = new RoomService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<ClassItem> cboClass;
    private JComboBox<RoomItem> cboRoom;
    private JDateChooser dcDate;
    private JTextField txtStartTime, txtEndTime;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnLoadByClass;
    private UserAccount currentUser;

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public SchedulePanel(UserAccount currentUser) {
        this.currentUser = currentUser;
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
                "Xếp Lịch Học", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Lớp học:"), gbc);
        gbc.gridx = 1;
        cboClass = new JComboBox<>();
        formPanel.add(cboClass, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Phòng:"), gbc);
        gbc.gridx = 3;
        cboRoom = new JComboBox<>();
        formPanel.add(cboRoom, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ngày học:"), gbc);
        gbc.gridx = 1;
        dcDate = DateUtil.createDateChooser();
        formPanel.add(dcDate, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Giờ BĐ (HH:mm):"), gbc);
        gbc.gridx = 3;
        txtStartTime = createTextField(6);
        txtStartTime.setToolTipText("VD: 08:00");
        formPanel.add(txtStartTime, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Giờ KT (HH:mm):"), gbc);
        gbc.gridx = 1;
        txtEndTime = createTextField(6);
        txtEndTime.setToolTipText("VD: 10:00");
        formPanel.add(txtEndTime, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnAdd = makeButton("✅ Thêm", new Color(46, 139, 87));
        btnUpdate = makeButton("✏️ Cập nhật", new Color(245, 158, 11));
        btnDelete = makeButton("❌ Xóa", new Color(178, 34, 34));
        btnClear = makeButton("🔄 Làm mới", new Color(70, 130, 180));
        btnLoadByClass = makeButton("👁️ Xem lịch lớp", new Color(138, 43, 226));
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnLoadByClass);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student) {
            btnAdd.setVisible(false);
            btnUpdate.setVisible(false);
            btnDelete.setVisible(false);
            btnClear.setVisible(false);
            dcDate.setEnabled(false);
            txtStartTime.setEditable(false);
            txtEndTime.setEditable(false);
            cboRoom.setEnabled(false);
        }

        add(formPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "Lớp", "Ngày học", "Bắt đầu", "Kết thúc", "Phòng" };
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

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel đếm Số lượng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JLabel lblTotal = new JLabel("Tổng bản ghi: 0");
        lblTotal.setName("lblTotal");
        bottomPanel.add(lblTotal);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());
        btnLoadByClass.addActionListener(e -> loadByClass());
    }

    public void refreshData() {
        loadCombos();
        try {
            if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student
                    && currentUser.getStudent() != null) {
                EnrollmentService es = new EnrollmentService();
                List<Long> classIds = es.findByStudent(currentUser.getStudent()).stream()
                        .map(e -> e.getClass_().getClassId())
                        .collect(Collectors.toList());
                List<Schedule> list = scheduleService.findAll().stream()
                        .filter(s -> s.getClass_() != null && classIds.contains(s.getClass_().getClassId()))
                        .collect(Collectors.toList());
                loadTable(list);
            } else if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Teacher
                    && currentUser.getTeacher() != null) {
                List<Long> classIds = classService.findByTeacherId(currentUser.getTeacher().getTeacherId()).stream()
                        .map(Class_::getClassId)
                        .collect(Collectors.toList());
                List<Schedule> list = scheduleService.findAll().stream()
                        .filter(s -> s.getClass_() != null && classIds.contains(s.getClass_().getClassId()))
                        .collect(Collectors.toList());
                loadTable(list);
            } else {
                loadTable(scheduleService.findAll());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadCombos() {
        cboClass.removeAllItems();
        try {
            List<Class_> classes;
            if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Teacher
                    && currentUser.getTeacher() != null) {
                classes = classService.findByTeacherId(currentUser.getTeacher().getTeacherId());
            } else if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Student
                    && currentUser.getStudent() != null) {
                EnrollmentService es = new EnrollmentService();
                classes = es.findByStudent(currentUser.getStudent()).stream()
                        .map(Enrollment::getClass_)
                        .collect(Collectors.toList());
            } else {
                classes = classService.findAll();
            }
            for (Class_ c : classes)
                cboClass.addItem(new ClassItem(c.getClassId(), c.getClassName()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }

        cboRoom.removeAllItems();
        try {
            for (Room r : roomService.findAllActive())
                cboRoom.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        } catch (Exception ignored) {
        }
    }

    private void loadTable(List<Schedule> list) {
        tableModel.setRowCount(0);
        for (Schedule s : list) {
            tableModel.addRow(new Object[] {
                    s.getScheduleId(),
                    s.getClass_() != null ? s.getClass_().getClassName() : "",
                    s.getStudyDate(),
                    s.getStartTime() != null ? s.getStartTime().format(TF) : "",
                    s.getEndTime() != null ? s.getEndTime().format(TF) : "",
                    s.getRoom() != null ? s.getRoom().getRoomName() : ""
            });
        }
        updateTotalLabel(list.size());
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

        setSelectedComboItem(cboClass, (String) tableModel.getValueAt(row, 1));
        setSelectedComboItem(cboRoom, (String) tableModel.getValueAt(row, 5));

        Object d = tableModel.getValueAt(row, 2);
        DateUtil.setLocalDate(dcDate, d instanceof LocalDate ? (LocalDate) d : null);
        txtStartTime.setText((String) tableModel.getValueAt(row, 3));
        txtEndTime.setText((String) tableModel.getValueAt(row, 4));
    }

    private void addSchedule() {
        try {
            Schedule s = new Schedule();
            ClassItem ci = (ClassItem) cboClass.getSelectedItem();
            if (ci != null)
                s.setClass_(classService.findById(ci.id).orElseThrow());
            RoomItem ri = (RoomItem) cboRoom.getSelectedItem();
            if (ri != null)
                s.setRoom(roomService.findById(ri.id).orElseThrow());
            LocalDate date = DateUtil.getLocalDate(dcDate);
            if (date == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày học.");
            s.setStudyDate(date);
            s.setStartTime(LocalTime.parse(txtStartTime.getText().trim(), TF));
            s.setEndTime(LocalTime.parse(txtEndTime.getText().trim(), TF));
            scheduleService.save(s);
            JOptionPane.showMessageDialog(this, "Thêm lịch thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSchedule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn lịch cần cập nhật.");
            return;
        }
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Schedule s = scheduleService.findById(id).orElseThrow();

            if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Teacher
                    && currentUser.getTeacher() != null && (s.getClass_() == null || s.getClass_().getTeacher() == null
                            || !s.getClass_().getTeacher().getTeacherId()
                                    .equals(currentUser.getTeacher().getTeacherId()))) {
                JOptionPane.showMessageDialog(this, "Bạn chỉ được cập nhật lịch của lớp do mình phụ trách.",
                        "Lỗi phân quyền", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ClassItem ci = (ClassItem) cboClass.getSelectedItem();
            if (ci != null)
                s.setClass_(classService.findById(ci.id).orElseThrow());
            RoomItem ri = (RoomItem) cboRoom.getSelectedItem();
            if (ri != null)
                s.setRoom(roomService.findById(ri.id).orElseThrow());
            LocalDate date = DateUtil.getLocalDate(dcDate);
            if (date == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày học.");
            s.setStudyDate(date);
            s.setStartTime(LocalTime.parse(txtStartTime.getText().trim(), TF));
            s.setEndTime(LocalTime.parse(txtEndTime.getText().trim(), TF));
            scheduleService.update(s);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSchedule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn lịch cần xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Schedule s = scheduleService.findById(id).orElseThrow();

            if (currentUser != null && currentUser.getRole() == UserAccount.UserRole.Teacher
                    && currentUser.getTeacher() != null && (s.getClass_() == null || s.getClass_().getTeacher() == null
                            || !s.getClass_().getTeacher().getTeacherId()
                                    .equals(currentUser.getTeacher().getTeacherId()))) {
                JOptionPane.showMessageDialog(this, "Bạn chỉ được xóa lịch của lớp do mình phụ trách.",
                        "Lỗi phân quyền", JOptionPane.WARNING_MESSAGE);
                return;
            }

            scheduleService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadByClass() {
        ClassItem ci = (ClassItem) cboClass.getSelectedItem();
        if (ci == null) {
            JOptionPane.showMessageDialog(this, "Chọn lớp trước.");
            return;
        }
        try {
            loadTable(scheduleService.findByClassId(ci.id));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void clearForm() {
        dcDate.setDate(null);
        txtStartTime.setText("");
        txtEndTime.setText("");
        table.clearSelection();
    }

    private record ClassItem(Long id, String name) {
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
