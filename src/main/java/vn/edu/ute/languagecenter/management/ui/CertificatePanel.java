package vn.edu.ute.languagecenter.management.ui;

import vn.edu.ute.languagecenter.management.model.Certificate;
import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.service.ClassService;
import vn.edu.ute.languagecenter.management.service.ExamAndCertService;
import vn.edu.ute.languagecenter.management.db.Jpa;

import com.toedter.calendar.JDateChooser;
import jakarta.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class CertificatePanel extends JPanel {

    private final ExamAndCertService service = new ExamAndCertService();
    private final ClassService classService = new ClassService();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<StudentItem> cboStudent;
    private JComboBox<ClassItem> cboClass;
    private JTextField txtCertName, txtSerialNo;
    private JDateChooser dcIssueDate;
    private JButton btnAdd, btnDelete, btnClear, btnRefresh;

    public CertificatePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Cấp chứng chỉ"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Học viên:"), gbc);
        gbc.gridx = 1;
        cboStudent = new JComboBox<>();
        formPanel.add(cboStudent, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Lớp học:"), gbc);
        gbc.gridx = 3;
        cboClass = new JComboBox<>();
        formPanel.add(cboClass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Tên chứng chỉ:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtCertName = new JTextField(20);
        formPanel.add(txtCertName, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Ngày cấp:"), gbc);
        gbc.gridx = 1;
        dcIssueDate = DateUtil.createDateChooser();
        DateUtil.setLocalDate(dcIssueDate, LocalDate.now());
        formPanel.add(dcIssueDate, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Số serial:"), gbc);
        gbc.gridx = 3;
        txtSerialNo = new JTextField(15);
        formPanel.add(txtSerialNo, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Cấp chứng chỉ");
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

        String[] cols = { "ID", "Học viên", "Lớp", "Tên chứng chỉ", "Ngày cấp", "Serial" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addCert());
        btnDelete.addActionListener(e -> deleteCert());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshData());
    }

    public void refreshData() {
        loadCombos();
        try {
            loadTable(service.findAllCerts());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadCombos() {
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

        cboClass.removeAllItems();
        try {
            for (Class_ c : classService.findAll())
                cboClass.addItem(new ClassItem(c.getClassId(), c.getClassName()));
        } catch (Exception ignored) {
        }
    }

    private void loadTable(List<Certificate> certs) {
        tableModel.setRowCount(0);
        for (Certificate c : certs) {
            tableModel.addRow(new Object[] {
                    c.getCertificateId(),
                    c.getStudent() != null ? c.getStudent().getFullName() : "",
                    c.getClass_() != null ? c.getClass_().getClassName() : "",
                    c.getCertName(), c.getIssueDate(), c.getSerialNo()
            });
        }
    }

    private void addCert() {
        try {
            Certificate c = new Certificate();
            StudentItem si = (StudentItem) cboStudent.getSelectedItem();
            if (si != null) {
                EntityManager em = Jpa.em();
                c.setStudent(em.find(Student.class, si.id));
                em.close();
            }
            ClassItem ci = (ClassItem) cboClass.getSelectedItem();
            if (ci != null)
                c.setClass_(classService.findById(ci.id).orElseThrow());
            c.setCertName(txtCertName.getText().trim());
            LocalDate issueDate = DateUtil.getLocalDate(dcIssueDate);
            if (issueDate == null)
                throw new IllegalArgumentException("Vui lòng chọn ngày cấp.");
            c.setIssueDate(issueDate);
            String serial = txtSerialNo.getText().trim();
            if (!serial.isEmpty())
                c.setSerialNo(serial);
            service.saveCertificate(c);
            JOptionPane.showMessageDialog(this, "Cấp chứng chỉ thành công!");
            clearForm();
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCert() {
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
            service.deleteCertById(id);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtCertName.setText("");
        txtSerialNo.setText("");
        DateUtil.setLocalDate(dcIssueDate, LocalDate.now());
        table.clearSelection();
    }

    private record StudentItem(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record ClassItem(Long id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}
