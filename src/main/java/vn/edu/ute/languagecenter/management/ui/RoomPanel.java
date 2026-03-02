package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.service.RoomService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {

    private final RoomService roomService = new RoomService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtCapacity, txtLocation, txtSearch;
    private JComboBox<String> cboStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;

    public RoomPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // ===== Form nhập liệu =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Phòng học"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên phòng:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(15);
        formPanel.add(txtName, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Sức chứa:"), gbc);
        gbc.gridx = 3;
        txtCapacity = new JTextField(8);
        formPanel.add(txtCapacity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Vị trí:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(15);
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3;
        cboStatus = new JComboBox<>(new String[] { "Active", "Inactive" });
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

        // ===== Table =====
        String[] cols = { "ID", "Tên phòng", "Sức chứa", "Vị trí", "Trạng thái" };
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
        btnAdd.addActionListener(e -> addRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchRoom());
    }

    public void refreshData() {
        try {
            loadTable(roomService.findAll());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[] { r.getRoomId(), r.getRoomName(), r.getCapacity(), r.getLocation(),
                    r.getStatus().name() });
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtCapacity.setText(tableModel.getValueAt(row, 2).toString());
        Object loc = tableModel.getValueAt(row, 3);
        txtLocation.setText(loc != null ? loc.toString() : "");
        cboStatus.setSelectedItem(tableModel.getValueAt(row, 4));
    }

    private void addRoom() {
        try {
            Room r = new Room();
            r.setRoomName(txtName.getText().trim());
            r.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
            r.setLocation(txtLocation.getText().trim());
            r.setStatus(Room.ActiveStatus.valueOf((String) cboStatus.getSelectedItem()));
            roomService.save(r);
            JOptionPane.showMessageDialog(this, "Thêm phòng thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn phòng cần cập nhật.");
            return;
        }
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Room r = roomService.findById(id).orElseThrow();
            r.setRoomName(txtName.getText().trim());
            r.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
            r.setLocation(txtLocation.getText().trim());
            r.setStatus(Room.ActiveStatus.valueOf((String) cboStatus.getSelectedItem()));
            roomService.update(r);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn phòng cần xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            roomService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchRoom() {
        String kw = txtSearch.getText().trim();
        try {
            loadTable(kw.isEmpty() ? roomService.findAll() : roomService.findByName(kw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtCapacity.setText("");
        txtLocation.setText("");
        txtSearch.setText("");
        cboStatus.setSelectedIndex(0);
        table.clearSelection();
    }
}
