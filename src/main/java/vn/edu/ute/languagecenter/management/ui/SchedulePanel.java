package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.model.Schedule;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.RoomService;
import vn.edu.ute.languagecenter.management.service.ScheduleService;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public SchedulePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Xếp lịch học"));
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
        txtStartTime = new JTextField(6);
        txtStartTime.setToolTipText("VD: 08:00");
        formPanel.add(txtStartTime, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Giờ KT (HH:mm):"), gbc);
        gbc.gridx = 1;
        txtEndTime = new JTextField(6);
        txtEndTime.setToolTipText("VD: 10:00");
        formPanel.add(txtEndTime, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm mới");
        btnLoadByClass = new JButton("Xem lịch lớp");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnLoadByClass);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "Lớp", "Ngày học", "Bắt đầu", "Kết thúc", "Phòng" };
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
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());
        btnLoadByClass.addActionListener(e -> loadByClass());
    }

    public void refreshData() {
        loadCombos();
        try {
            loadTable(scheduleService.findAll());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadCombos() {
        cboClass.removeAllItems();
        try {
            for (Class_ c : classService.findAll())
                cboClass.addItem(new ClassItem(c.getClassId(), c.getClassName()));
        } catch (Exception ignored) {
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
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
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
            JOptionPane.showMessageDialog(this, "Thêm lịch thành công!");
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
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
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
            scheduleService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
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
}
