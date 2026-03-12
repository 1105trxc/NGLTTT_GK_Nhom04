package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CourseSelectionDialog extends JDialog {

    private final List<Course> courses;
    private Course selectedCourse = null;

    private JTextField txtSearch;
    private JTable tblCourse;
    private DefaultTableModel tableModel;

    public CourseSelectionDialog(Window owner, List<Course> courses) {
        super(owner, "Chọn Khóa Học", ModalityType.APPLICATION_MODAL);
        this.courses = courses;

        setSize(600, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(courses);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm khóa học:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        String[] cols = {"ID", "Tên Khóa Học", "Cấp Độ", "Học Phí"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCourse = new JTable(tableModel);
        tblCourse.setRowHeight(25);
        tblCourse.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblCourse.getColumnModel().getColumn(0).setMinWidth(0);
        tblCourse.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblCourse), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        tblCourse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Course> list) {
        tableModel.setRowCount(0);
        for (Course c : list) {
            tableModel.addRow(new Object[]{
                    c.getCourseId(), c.getCourseName(), c.getLevel(), c.getFee()
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        List<Course> filtered = kw.isEmpty() ? courses : courses.stream()
                .filter(c -> c.getCourseName().toLowerCase().contains(kw)).toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblCourse.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            selectedCourse = courses.stream().filter(c -> c.getCourseId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Course getSelectedCourse() { return selectedCourse; }
}