package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StudentSelectionDialog extends JDialog {

    private final List<Student> students;
    private Student selectedStudent = null;

    private JTextField txtSearch;
    private JTable tblStudent;
    private DefaultTableModel tableModel;

    public StudentSelectionDialog(Window owner, List<Student> students) {
        super(owner, "Chọn Học Viên", ModalityType.APPLICATION_MODAL);
        this.students = students;

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(students); // Load tất cả lúc mới mở
    }

    private void buildUI() {
        // --- Phần tìm kiếm (Top) ---
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm theo Tên / SĐT:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        // Bắt sự kiện gõ phím để Live Search
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        // --- Phần Bảng (Center) ---
        String[] cols = {"ID", "Họ Tên", "SĐT", "Email"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStudent = new JTable(tableModel);
        tblStudent.setRowHeight(25);
        tblStudent.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Ẩn cột ID đi cho đẹp
        tblStudent.getColumnModel().getColumn(0).setMinWidth(0);
        tblStudent.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblStudent), BorderLayout.CENTER);

        // --- Phần Nút bấm (Bottom) ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        // Sự kiện nháy đúp chuột vào bảng để chọn luôn cho nhanh
        tblStudent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Student> list) {
        tableModel.setRowCount(0);
        for (Student s : list) {
            tableModel.addRow(new Object[]{
                    s.getStudentId(), s.getFullName(),
                    s.getPhone() != null ? s.getPhone() : "",
                    s.getEmail() != null ? s.getEmail() : ""
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(students);
            return;
        }
        // Lọc học viên theo tên hoặc số điện thoại
        List<Student> filtered = students.stream()
                .filter(s -> s.getFullName().toLowerCase().contains(kw)
                        || (s.getPhone() != null && s.getPhone().contains(kw)))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblStudent.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            // Tìm lại Student từ list ban đầu
            selectedStudent = students.stream().filter(s -> s.getStudentId().equals(id)).findFirst().orElse(null);
            dispose(); // Đóng cửa sổ
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một học viên trên bảng!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Hàm này để lớp cha (EnrollmentPanel) gọi ra lấy kết quả
    public Student getSelectedStudent() {
        return selectedStudent;
    }
}