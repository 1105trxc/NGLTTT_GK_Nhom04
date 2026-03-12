package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Teacher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TeacherSelectionDialog extends JDialog {

    private final List<Teacher> teachers;
    private Teacher selectedTeacher = null;

    private JTextField txtSearch;
    private JTable tblTeacher;
    private DefaultTableModel tableModel;

    public TeacherSelectionDialog(Window owner, List<Teacher> teachers) {
        super(owner, "Chọn Giáo Viên", ModalityType.APPLICATION_MODAL);
        this.teachers = teachers;

        setSize(600, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(teachers);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm tên/SĐT:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);

        // Nút Hủy Chọn (để gỡ giáo viên khỏi lớp)
        JButton btnClear = new JButton("Xóa Trống");
        btnClear.addActionListener(e -> {
            selectedTeacher = null;
            dispose();
        });
        pnlSearch.add(btnClear, BorderLayout.EAST);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterData();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterData();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterData();
            }
        });

        String[] cols = {"ID", "Họ Tên", "Số điện thoại", "Chuyên môn"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTeacher = new JTable(tableModel);
        tblTeacher.setRowHeight(25);
        tblTeacher.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblTeacher.getColumnModel().getColumn(0).setMinWidth(0);
        tblTeacher.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblTeacher), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        tblTeacher.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Teacher> list) {
        tableModel.setRowCount(0);
        for (Teacher t : list) {
            tableModel.addRow(new Object[]{
                    t.getTeacherId(), t.getFullName(),
                    t.getPhone() != null ? t.getPhone() : "",
                    t.getSpecialty() != null ? t.getSpecialty() : ""
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        List<Teacher> filtered = kw.isEmpty() ? teachers : teachers.stream()
                .filter(t -> t.getFullName().toLowerCase().contains(kw)
                        || (t.getPhone() != null && t.getPhone().contains(kw))).toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblTeacher.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            selectedTeacher = teachers.stream().filter(t -> t.getTeacherId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giáo viên!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Teacher getSelectedTeacher() {
        return selectedTeacher;
    }
}