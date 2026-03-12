package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Class_;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClassSelectionDialog extends JDialog {

    private final List<Class_> classes;
    private Class_ selectedClass = null;

    private JTextField txtSearch;
    private JTable tblClass;
    private DefaultTableModel tableModel;

    public ClassSelectionDialog(Window owner, List<Class_> classes) {
        super(owner, "Chọn Lớp Học", ModalityType.APPLICATION_MODAL);
        this.classes = classes;

        setSize(700, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(classes);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm tên lớp:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        add(pnlSearch, BorderLayout.NORTH);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        // Bảng hiển thị thông tin lớp chi tiết
        String[] cols = {"ID", "Tên Lớp", "Khóa Học", "Khai Giảng", "Trạng Thái", "Sĩ số Tối Đa"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblClass = new JTable(tableModel);
        tblClass.setRowHeight(25);
        tblClass.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        tblClass.getColumnModel().getColumn(0).setMinWidth(0);
        tblClass.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblClass), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        tblClass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Class_> list) {
        tableModel.setRowCount(0);
        for (Class_ c : list) {
            tableModel.addRow(new Object[]{
                    c.getClassId(),
                    c.getClassName(),
                    c.getCourse() != null ? c.getCourse().getCourseName() : "",
                    c.getStartDate(),
                    c.getStatus(),
                    c.getMaxStudent()
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(classes);
            return;
        }
        List<Class_> filtered = classes.stream()
                .filter(c -> c.getClassName().toLowerCase().contains(kw))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblClass.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            selectedClass = classes.stream().filter(c -> c.getClassId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp học!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Class_ getSelectedClass() {
        return selectedClass;
    }
}