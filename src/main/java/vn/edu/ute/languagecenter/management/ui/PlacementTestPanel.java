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
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Kiểm tra đầu vào (Placement Test)"));
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
        txtScore = new JTextField(6);
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
        txtNote = new JTextField(25);
        formPanel.add(txtNote, gbc);
        gbc.gridwidth = 1;

        txtScore.addActionListener(e -> updateSuggestedLevel());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Lưu kết quả");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm mới");
        btnRefresh = new JButton("Tải lại");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnRefresh);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "Học viên", "Ngày thi", "Điểm", "Cấp độ gợi ý", "Ghi chú" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

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
            List<Student> students = em.createQuery("SELECT s FROM Student s WHERE s.status = 'Active'", Student.class)
                    .getResultList();
            em.close();
            for (Student s : students)
                cboStudent.addItem(new StudentItem(s.getStudentId(), s.getFullName()));
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
            JOptionPane.showMessageDialog(this, "Lưu kết quả thành công!");
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
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
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
}
