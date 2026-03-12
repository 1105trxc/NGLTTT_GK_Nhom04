package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.model.Schedule;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.RoomService;
import vn.edu.ute.languagecenter.management.service.ScheduleService;
import vn.edu.ute.languagecenter.management.ui.gui_finance.ClassSelectionDialog;
import vn.edu.ute.languagecenter.management.ui.gui_finance.RoomSelectionDialog;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SchedulePanel extends JPanel {

    // ── Services ──────────────────────────────────────────────────────────────
    private final ScheduleService scheduleService = new ScheduleService();
    private final ClassService classService = new ClassService();
    private final RoomService roomService = new RoomService();

    // ── Components ────────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;

    // Nút chọn Lớp học
    private Class_ selectedClass = null;
    private JTextField txtClassName;
    private JButton btnSelectClass;

    // Nút chọn Phòng học
    private Room selectedRoom = null;
    private JTextField txtRoomName;
    private JButton btnSelectRoom;

    private JDateChooser dcDate;
    private JSpinner spnStartTime, spnEndTime; // Đã thay bằng Spinner

    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnLoadByClass;
    private Long teacherId;

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public SchedulePanel(Long teacherId) {
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
                "Xếp Lịch Học", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. CHỌN LỚP HỌC
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Lớp học:"), gbc);

        txtClassName = createTextField(15);
        txtClassName.setEditable(false);
        txtClassName.setText("Chưa chọn lớp...");
        btnSelectClass = new JButton("🔍");
        JPanel pnlClassSelect = new JPanel(new BorderLayout(5, 0));
        pnlClassSelect.setOpaque(false);
        pnlClassSelect.add(txtClassName, BorderLayout.CENTER);
        pnlClassSelect.add(btnSelectClass, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(pnlClassSelect, gbc);

        // 2. CHỌN PHÒNG HỌC
        gbc.gridx = 2;
        formPanel.add(new JLabel("Phòng:"), gbc);

        txtRoomName = createTextField(15);
        txtRoomName.setEditable(false);
        txtRoomName.setText("Chưa chọn phòng...");
        btnSelectRoom = new JButton("🔍");
        JPanel pnlRoomSelect = new JPanel(new BorderLayout(5, 0));
        pnlRoomSelect.setOpaque(false);
        pnlRoomSelect.add(txtRoomName, BorderLayout.CENTER);
        pnlRoomSelect.add(btnSelectRoom, BorderLayout.EAST);

        gbc.gridx = 3;
        formPanel.add(pnlRoomSelect, gbc);

        // 3. THÔNG TIN NGÀY VÀ GIỜ (DÙNG SPINNER)
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ngày học:"), gbc);
        gbc.gridx = 1;
        dcDate = DateUtil.createDateChooser();
        formPanel.add(dcDate, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Giờ BĐ (HH:mm):"), gbc);
        gbc.gridx = 3;
        spnStartTime = createTimeSpinner();
        setSpinnerTime(spnStartTime, LocalTime.of(8, 0)); // Mặc định 08:00
        formPanel.add(spnStartTime, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Giờ KT (HH:mm):"), gbc);
        gbc.gridx = 1;
        spnEndTime = createTimeSpinner();
        setSpinnerTime(spnEndTime, LocalTime.of(10, 0)); // Mặc định 10:00
        formPanel.add(spnEndTime, gbc);

        // --- BUTTONS ---
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

        add(formPanel, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU ---
        String[] cols = {"ID", "Lớp", "Ngày học", "Bắt đầu", "Kết thúc", "Phòng"};
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

        // --- GẮN SỰ KIỆN ---
        btnSelectClass.addActionListener(e -> openClassDialog());
        btnSelectRoom.addActionListener(e -> openRoomDialog());

        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());
        btnLoadByClass.addActionListener(e -> loadByClass());
    }

    private void openClassDialog() {
        try {
            List<Class_> classes = teacherId != null ? classService.findByTeacherId(teacherId) : classService.findAll();
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            ClassSelectionDialog dialog = new ClassSelectionDialog(parentWindow, classes);
            dialog.setVisible(true);

            Class_ c = dialog.getSelectedClass();
            if (c != null) {
                selectedClass = c;
                txtClassName.setText(c.getClassName());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải lớp học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRoomDialog() {
        try {
            List<Room> rooms = roomService.findAllActive();
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            RoomSelectionDialog dialog = new RoomSelectionDialog(parentWindow, rooms);
            dialog.setVisible(true);

            Room r = dialog.getSelectedRoom();
            if (r != null) {
                selectedRoom = r;
                txtRoomName.setText(r.getRoomName());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải phòng học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        try {
            loadTable(scheduleService.findAll());
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadTable(List<Schedule> list) {
        tableModel.setRowCount(0);
        for (Schedule s : list) {
            tableModel.addRow(new Object[]{
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

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Schedule s = scheduleService.findById(id).orElse(null);

            if (s != null) {
                if (s.getClass_() != null) {
                    selectedClass = s.getClass_();
                    txtClassName.setText(selectedClass.getClassName());
                } else {
                    selectedClass = null;
                    txtClassName.setText("Chưa chọn lớp...");
                }

                if (s.getRoom() != null) {
                    selectedRoom = s.getRoom();
                    txtRoomName.setText(selectedRoom.getRoomName());
                } else {
                    selectedRoom = null;
                    txtRoomName.setText("Chưa chọn phòng...");
                }

                DateUtil.setLocalDate(dcDate, s.getStudyDate());
                if (s.getStartTime() != null) setSpinnerTime(spnStartTime, s.getStartTime());
                if (s.getEndTime() != null) setSpinnerTime(spnEndTime, s.getEndTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSchedule() {
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng học!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Schedule s = new Schedule();
            s.setClass_(selectedClass);
            s.setRoom(selectedRoom);

            LocalDate date = DateUtil.getLocalDate(dcDate);
            if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày học.");

            s.setStudyDate(date);
            s.setStartTime(getSpinnerTime(spnStartTime));
            s.setEndTime(getSpinnerTime(spnEndTime));

            scheduleService.save(s);
            JOptionPane.showMessageDialog(this, "Thêm lịch thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
        if (selectedClass == null || selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Lớp và Phòng!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Schedule s = scheduleService.findById(id).orElseThrow(() -> new Exception("Không tìm thấy lịch học trong CSDL"));

            if (teacherId != null && (s.getClass_() == null || s.getClass_().getTeacher() == null
                    || !s.getClass_().getTeacher().getTeacherId().equals(teacherId))) {
                JOptionPane.showMessageDialog(this, "Bạn chỉ được cập nhật lịch của lớp do mình phụ trách.",
                        "Lỗi phân quyền", JOptionPane.WARNING_MESSAGE);
                return;
            }

            s.setClass_(selectedClass);
            s.setRoom(selectedRoom);

            LocalDate date = DateUtil.getLocalDate(dcDate);
            if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày học.");

            s.setStudyDate(date);
            s.setStartTime(getSpinnerTime(spnStartTime));
            s.setEndTime(getSpinnerTime(spnEndTime));

            scheduleService.update(s);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
            Schedule s = scheduleService.findById(id).orElseThrow(() -> new Exception("Không tìm thấy lịch học"));

            if (teacherId != null && (s.getClass_() == null || s.getClass_().getTeacher() == null
                    || !s.getClass_().getTeacher().getTeacherId().equals(teacherId))) {
                JOptionPane.showMessageDialog(this, "Bạn chỉ được xóa lịch của lớp do mình phụ trách.",
                        "Lỗi phân quyền", JOptionPane.WARNING_MESSAGE);
                return;
            }

            scheduleService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadByClass() {
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp trước bằng nút 🔍.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            loadTable(scheduleService.findByClassId(selectedClass.getClassId()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedClass = null;
        txtClassName.setText("Chưa chọn lớp...");
        selectedRoom = null;
        txtRoomName.setText("Chưa chọn phòng...");

        dcDate.setDate(null);
        setSpinnerTime(spnStartTime, LocalTime.of(8, 0));
        setSpinnerTime(spnEndTime, LocalTime.of(10, 0));
        table.clearSelection();
    }

    // ── Các hàm tiện ích UI ───────────────────────────────────────────────────

    private JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(100, 28));
        return spinner;
    }

    private LocalTime getSpinnerTime(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
    }

    private void setSpinnerTime(JSpinner spinner, LocalTime time) {
        if (time == null) return;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(java.util.Calendar.MINUTE, time.getMinute());
        cal.set(java.util.Calendar.SECOND, 0);
        spinner.setValue(cal.getTime());
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