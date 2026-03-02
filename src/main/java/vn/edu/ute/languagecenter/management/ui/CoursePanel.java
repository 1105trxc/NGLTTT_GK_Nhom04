package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Course;
import vn.edu.ute.languagecenter.management.service.CourseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class CoursePanel extends JPanel {

    private final CourseService courseService = new CourseService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtDescription, txtDuration, txtFee, txtSearch;
    private JComboBox<String> cboLevel, cboDurationUnit, cboStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;

    public CoursePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // ===== TOP: Form nhập liệu =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Khóa học"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Tên khóa học
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên khóa học:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtName = new JTextField(30);
        formPanel.add(txtName, gbc);
        gbc.gridwidth = 1;

        // Row 1: Mô tả
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtDescription = new JTextField(30);
        formPanel.add(txtDescription, gbc);
        gbc.gridwidth = 1;

        // Row 2: Level + Duration
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Cấp độ:"), gbc);
        gbc.gridx = 1;
        cboLevel = new JComboBox<>(new String[] { "Beginner", "Intermediate", "Advanced" });
        formPanel.add(cboLevel, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Thời lượng:"), gbc);
        gbc.gridx = 3;
        txtDuration = new JTextField(8);
        formPanel.add(txtDuration, gbc);

        // Row 3: Duration Unit + Fee
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1;
        cboDurationUnit = new JComboBox<>(new String[] { "Week", "Hour" });
        formPanel.add(cboDurationUnit, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Học phí:"), gbc);
        gbc.gridx = 3;
        txtFee = new JTextField(10);
        formPanel.add(txtFee, gbc);

        // Row 4: Status
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cboStatus = new JComboBox<>(new String[] { "Active", "Inactive" });
        formPanel.add(cboStatus, gbc);

        // ===== Buttons =====
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

        // ===== CENTER: Table =====
        String[] columns = { "ID", "Tên khóa học", "Cấp độ", "Thời lượng", "Đơn vị", "Học phí", "Trạng thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                fillForm();
        });

        // Search panel
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

        // ===== Event handlers =====
        btnAdd.addActionListener(e -> addCourse());
        btnUpdate.addActionListener(e -> updateCourse());
        btnDelete.addActionListener(e -> deleteCourse());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchCourse());
    }

    public void refreshData() {
        try {
            loadTable(courseService.findAll());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadTable(List<Course> courses) {
        tableModel.setRowCount(0);
        for (Course c : courses) {
            tableModel.addRow(new Object[] {
                    c.getCourseId(),
                    c.getCourseName(),
                    c.getLevel() != null ? c.getLevel().name() : "",
                    c.getDuration(),
                    c.getDurationUnit() != null ? c.getDurationUnit().name() : "",
                    c.getFee(),
                    c.getStatus().name()
            });
        }
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        txtName.setText((String) tableModel.getValueAt(row, 1));
        String level = (String) tableModel.getValueAt(row, 2);
        if (!level.isEmpty())
            cboLevel.setSelectedItem(level);
        Object dur = tableModel.getValueAt(row, 3);
        txtDuration.setText(dur != null ? dur.toString() : "");
        String unit = (String) tableModel.getValueAt(row, 4);
        if (!unit.isEmpty())
            cboDurationUnit.setSelectedItem(unit);
        Object fee = tableModel.getValueAt(row, 5);
        txtFee.setText(fee != null ? fee.toString() : "");
        cboStatus.setSelectedItem(tableModel.getValueAt(row, 6));
    }

    private Course buildCourseFromForm() {
        Course c = new Course();
        c.setCourseName(txtName.getText().trim());
        c.setDescription(txtDescription.getText().trim());
        c.setLevel(Course.Level.valueOf((String) cboLevel.getSelectedItem()));
        String dur = txtDuration.getText().trim();
        if (!dur.isEmpty())
            c.setDuration(Integer.parseInt(dur));
        c.setDurationUnit(Course.DurationUnit.valueOf((String) cboDurationUnit.getSelectedItem()));
        c.setFee(new BigDecimal(txtFee.getText().trim()));
        c.setStatus(Course.ActiveStatus.valueOf((String) cboStatus.getSelectedItem()));
        return c;
    }

    private void addCourse() {
        try {
            Course c = buildCourseFromForm();
            courseService.save(c);
            JOptionPane.showMessageDialog(this, "Thêm khóa học thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn khóa học cần cập nhật.");
            return;
        }
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            Course c = courseService.findById(id).orElseThrow();
            c.setCourseName(txtName.getText().trim());
            c.setDescription(txtDescription.getText().trim());
            c.setLevel(Course.Level.valueOf((String) cboLevel.getSelectedItem()));
            String dur = txtDuration.getText().trim();
            if (!dur.isEmpty())
                c.setDuration(Integer.parseInt(dur));
            c.setDurationUnit(Course.DurationUnit.valueOf((String) cboDurationUnit.getSelectedItem()));
            c.setFee(new BigDecimal(txtFee.getText().trim()));
            c.setStatus(Course.ActiveStatus.valueOf((String) cboStatus.getSelectedItem()));
            courseService.update(c);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn khóa học cần xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            courseService.deleteById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCourse() {
        String keyword = txtSearch.getText().trim();
        try {
            if (keyword.isEmpty()) {
                refreshData();
            } else {
                loadTable(courseService.findByName(keyword));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtDescription.setText("");
        txtDuration.setText("");
        txtFee.setText("");
        txtSearch.setText("");
        cboLevel.setSelectedIndex(0);
        cboDurationUnit.setSelectedIndex(0);
        cboStatus.setSelectedIndex(0);
        table.clearSelection();
    }
}
