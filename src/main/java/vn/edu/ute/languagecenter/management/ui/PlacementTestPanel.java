package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.PlacementTest;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.service.ExamAndCertService;
import vn.edu.ute.languagecenter.management.db.Jpa;

import com.toedter.calendar.JDateChooser;
import jakarta.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PlacementTestPanel extends JPanel {

    private final ExamAndCertService service = new ExamAndCertService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<StudentItem> cboStudent;
    private JDateChooser dcDate;
    private JTextField txtScore, txtNote;
    private JLabel lblSuggestedLevel;
    private JButton btnAdd, btnDelete, btnClear, btnRefresh;

    public PlacementTestPanel() {
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
                "Kiểm Tra Đầu Vào (Placement Test)", javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Học viên:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cboStudent = new JComboBox<>();
        formPanel.add(cboStudent, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ngày thi:"), gbc);
        gbc.gridx = 1;
        dcDate = DateUtil.createDateChooser();
        DateUtil.setLocalDate(dcDate, LocalDate.now());
        formPanel.add(dcDate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Điểm (0-10):"), gbc);
        gbc.gridx = 1;
        txtScore = createTextField(6);
        formPanel.add(txtScore, gbc);

        gbc.gridx = 2;
        lblSuggestedLevel = new JLabel("Cấp độ gợi ý: ---");
        lblSuggestedLevel.setForeground(new Color(0, 100, 0));
        formPanel.add(lblSuggestedLevel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtNote = createTextField(25);
        formPanel.add(txtNote, gbc);
        gbc.gridwidth = 1;

        txtScore.addActionListener(e -> updateSuggestedLevel());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.setOpaque(false);
        btnAdd = makeButton("✅ Lưu kết quả", new Color(46, 139, 87));
        btnDelete = makeButton("❌ Xóa", new Color(178, 34, 34));
        btnClear = makeButton("🧹 Làm mới", new Color(70, 130, 180));
        btnRefresh = makeButton("🔄 Tải lại", new Color(245, 158, 11));
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "Học viên", "Ngày thi", "Điểm", "Cấp độ gợi ý", "Ghi chú" };
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

        btnAdd.addActionListener(e -> addTest());
        btnDelete.addActionListener(e -> deleteTest());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshData());
    }

    public void refreshData() {
        loadStudents();
        try {
            loadTable(service.findAllTests());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadStudents() {
        cboStudent.removeAllItems();
        try {
            EntityManager em = Jpa.em();
            List<Student> students = em.createQuery("SELECT s FROM Student s", Student.class).getResultList();
            em.close();
            for (Student s : students) {
                if (s.getStatus() != null && s.getStatus().name().equals("Active")) {
                    cboStudent.addItem(new StudentItem(s.getStudentId(), s.getFullName()));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void loadTable(List<PlacementTest> tests) {
        tableModel.setRowCount(0);
        for (PlacementTest t : tests) {
            tableModel.addRow(new Object[] {
                    t.getTestId(),
                    t.getStudent() != null ? t.getStudent().getFullName() : "",
                    t.getTestDate(), t.getScore(), t.getSuggestedLevel(), t.getNote()
            });
        }
        updateTotalLabel(tests.size());
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
            PlacementTest t = service.findTestById(id).orElse(null);
            if (t != null) {
                Student s = t.getStudent();
                if (s != null) {
                    for (int i = 0; i < cboStudent.getItemCount(); i++) {
                        if (cboStudent.getItemAt(i).id().equals(s.getStudentId())) {
                            cboStudent.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                DateUtil.setLocalDate(dcDate, t.getTestDate());
                txtScore.setText(t.getScore() != null ? t.getScore().toString() : "");
                updateSuggestedLevel();
                txtNote.setText(t.getNote() != null ? t.getNote() : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSuggestedLevel() {
        try {
            double score = Double.parseDouble(txtScore.getText().trim());
            String level = score >= 7 ? "Advanced" : score >= 4 ? "Intermediate" : "Beginner";
            lblSuggestedLevel.setText("Cấp độ gợi ý: " + level);
        } catch (NumberFormatException ex) {
            lblSuggestedLevel.setText("Cấp độ gợi ý: ---");
        }
    }

    private void addTest() {
        try {
            PlacementTest t = new PlacementTest();
            StudentItem si = (StudentItem) cboStudent.getSelectedItem();
            if (si != null) {
                EntityManager em = Jpa.em();
                t.setStudent(em.find(Student.class, si.id));
                em.close();
            }
            LocalDate date = DateUtil.getLocalDate(dcDate);
            if (date == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày thi.");
            t.setTestDate(date);
            String sc = txtScore.getText().trim();
            if (!sc.isEmpty())
                t.setScore(new BigDecimal(sc));
            t.setNote(txtNote.getText().trim());
            service.saveTest(t);
            JOptionPane.showMessageDialog(this, "Lưu kết quả thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTest() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            Long id = (Long) tableModel.getValueAt(row, 0);
            service.deleteTestById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        DateUtil.setLocalDate(dcDate, LocalDate.now());
        txtScore.setText("");
        txtNote.setText("");
        lblSuggestedLevel.setText("Cấp độ gợi ý: ---");
        table.clearSelection();
    }

    private record StudentItem(Long id, String name) {
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
