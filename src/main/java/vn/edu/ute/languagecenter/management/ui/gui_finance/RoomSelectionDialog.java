package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RoomSelectionDialog extends JDialog {

    private final List<Room> rooms;
    private Room selectedRoom = null;

    private JTextField txtSearch;
    private JTable tblRoom;
    private DefaultTableModel tableModel;

    public RoomSelectionDialog(Window owner, List<Room> rooms) {
        super(owner, "Chọn Phòng Học", ModalityType.APPLICATION_MODAL);
        this.rooms = rooms;

        setSize(500, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        buildUI();
        loadDataToTable(rooms);
    }

    private void buildUI() {
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.add(new JLabel("🔍 Tìm tên phòng:"), BorderLayout.WEST);
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

        String[] cols = {"ID", "Tên Phòng", "Sức Chứa", "Vị Trí"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRoom = new JTable(tableModel);
        tblRoom.setRowHeight(25);
        tblRoom.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        tblRoom.getColumnModel().getColumn(0).setMinWidth(0);
        tblRoom.getColumnModel().getColumn(0).setMaxWidth(0);

        add(new JScrollPane(tblRoom), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSelect = new JButton("✅ Chọn");
        JButton btnCancel = new JButton("❌ Hủy");
        pnlButtons.add(btnSelect);
        pnlButtons.add(btnCancel);
        add(pnlButtons, BorderLayout.SOUTH);

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        tblRoom.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) confirmSelection();
            }
        });
    }

    private void loadDataToTable(List<Room> list) {
        tableModel.setRowCount(0);
        for (Room r : list) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomName(),
                    r.getCapacity(),
                    r.getLocation() != null ? r.getLocation() : ""
            });
        }
    }

    private void filterData() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadDataToTable(rooms);
            return;
        }
        List<Room> filtered = rooms.stream()
                .filter(r -> r.getRoomName().toLowerCase().contains(kw))
                .toList();
        loadDataToTable(filtered);
    }

    private void confirmSelection() {
        int row = tblRoom.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) tableModel.getValueAt(row, 0);
            selectedRoom = rooms.stream().filter(r -> r.getRoomId().equals(id)).findFirst().orElse(null);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng học!", "Chú ý", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Room getSelectedRoom() {
        return selectedRoom;
    }
}