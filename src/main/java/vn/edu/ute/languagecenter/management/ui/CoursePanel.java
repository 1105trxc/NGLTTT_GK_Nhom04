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
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // ===== TOP: Form nhập liệu =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Thông Tin Khóa Học", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Tên khóa học
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên khóa học:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtName = createTextField(30);
        formPanel.add(txtName, gbc);
        gbc.gridwidth = 1;

        // Row 1: Mô tả
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtDescription = createTextField(30);
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
        txtDuration = createTextField(8);
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
        txtFee = createTextField(10);
        formPanel.add(txtFee, gbc);

        // Row 4: Status
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cboStatus = new JComboBox<>(new String[] { "Active", "Inactive" });
        formPanel.add(cboStatus, gbc);

        // ===== Buttons =====
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

        // Đưa nút bấm vào Row 5 trong Form
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        // Đẩy formPanel lên trên cùng
        add(formPanel, BorderLayout.NORTH);

        // ===== CENTER: Table =====
        String[] columns = { "ID", "Tên khóa học", "Cấp độ", "Thời lượng", "Đơn vị", "Học phí", "Trạng thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
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

        // Search panel
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
        updateTotalLabel(courses.size());
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
        if (row < 0)
            return;
        Long id = (Long) tableModel.getValueAt(row, 0);
        try {
            Course c = courseService.findById(id).orElse(null);
            if (c != null) {
                txtName.setText(c.getCourseName());
                txtDescription.setText(c.getDescription() != null ? c.getDescription() : "");
                cboLevel.setSelectedItem(c.getLevel() != null ? c.getLevel().name() : "");
                txtDuration.setText(c.getDuration() != null ? String.valueOf(c.getDuration()) : "");
                cboDurationUnit.setSelectedItem(c.getDurationUnit() != null ? c.getDurationUnit().name() : "");
                txtFee.setText(c.getFee() != null ? c.getFee().toString() : "");
                cboStatus.setSelectedItem(c.getStatus() != null ? c.getStatus().name() : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
