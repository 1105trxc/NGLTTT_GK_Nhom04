package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Branch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BranchSelectionDialog extends JDialog {

    private final List<Branch> branches;
    private Branch selectedBranch = null;

    private JTextField txtSearch;
    private JTable tblBranch;
    private DefaultTableModel tableModel;

    public BranchSelectionDialog(Window owner, List<Branch> branches) {
        super(owner, "Chọn Chi Nhánh", ModalityType.APPLICATION_MODAL);
        this.branches = branches;

        setSize(500, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(branches);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm tên chi nhánh:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
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

        String[] cols = {"ID", "Tên Chi Nhánh", "Địa Chỉ", "Điện Thoại"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBranch = new JTable(tableModel);
        tblBranch.setRowHeight(25);
        tblBranch.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        tblBranch.getColumnModel().getColumn(0).setMinWidth(0);
        tblBranch.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblBranch), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        tblBranch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Branch> list) {
        tableModel.setRowCount(0);
        for (Branch b : list) {
            tableModel.addRow(new Object[]{
                    b.getBranchId(),
                    b.getBranchName(),
                    b.getAddress() != null ? b.getAddress() : "",
                    b.getPhone() != null ? b.getPhone() : ""
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(branches);
            return;
        }
        List<Branch> filtered = branches.stream()
                .filter(b -> b.getBranchName().toLowerCase().contains(kw))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblBranch.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            selectedBranch = branches.stream().filter(b -> b.getBranchId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi nhánh!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Branch getSelectedBranch() {
        return selectedBranch;
    }
}
