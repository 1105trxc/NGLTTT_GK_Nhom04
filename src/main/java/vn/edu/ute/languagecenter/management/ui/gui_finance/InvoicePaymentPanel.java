package vn.edu.ute.languagecenter.management.ui.gui_finance;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Payment;
import vn.edu.ute.languagecenter.management.model.Promotion;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.PaymentRepository;
import vn.edu.ute.languagecenter.management.repo.PromotionRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaClassRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaPaymentRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaPromotionRepository;
import vn.edu.ute.languagecenter.management.service.InvoiceService;
import vn.edu.ute.languagecenter.management.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InvoicePaymentPanel extends JPanel {

    // ── Services / Repos ──────────────────────────────────────────────────────
    private final InvoiceService invoiceService = new InvoiceService();
    private final StudentService studentService = new StudentService();
    private final JpaClassRepository classRepo = new JpaClassRepository();
    private final PaymentRepository paymentRepo = new JpaPaymentRepository();
    private final PromotionRepository promotionRepo = new JpaPromotionRepository();

    // ── NORTH ─────────────────────────────────────────────────────────────────
    private JComboBox<Student> cboStudent;
    private JComboBox<Class_> cboClass;
    private JComboBox<Promotion> cboPromotion;
    private JLabel lblOriginalFee;
    private JLabel lblDiscount;
    private JLabel lblTotal;
    private JButton btnCreateInvoice;

    // ── CENTER ────────────────────────────────────────────────────────────────
    private JTable tblInvoice;
    private DefaultTableModel invoiceModel;

    // ── EAST ──────────────────────────────────────────────────────────────────
    private JLabel lblInvoiceDetail;
    private JLabel lblPaidSoFar;
    private JLabel lblRemaining;
    private JTextField txtAmount;
    private JComboBox<String> cboMethod;
    private JTextField txtRef;
    private JButton btnPay;
    private JButton btnMarkPaid;

    public InvoicePaymentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);
        add(buildCreatePanel(), BorderLayout.NORTH);
        add(buildInvoiceTable(), BorderLayout.CENTER);
        add(buildPaymentPanel(), BorderLayout.EAST);

        // Tự load dữ liệu khi khởi tạo
        refreshComboData();
        loadInvoiceTable();
    }

    // ── Tự load Student, Class, Promotion ────────────────────────────────────
    private void refreshComboData() {
        try {
            List<Student> students = studentService.getActiveStudents();
            cboStudent.removeAllItems();
            students.forEach(cboStudent::addItem);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không load được danh sách học viên: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        try {
            List<Class_> classes = classRepo.findAll();
            cboClass.removeAllItems();
            classes.forEach(cboClass::addItem);
            recalcPreview();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không load được danh sách lớp: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        loadPromotions();
    }

    private JPanel buildCreatePanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(new Color(255, 250, 240));
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(218, 165, 32), 1),
                "Tạo Hóa Đơn Học Phí", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(139, 90, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        addLabel(pnl, "Học viên:", g, 0, row);
        cboStudent = new JComboBox<>();
        cboStudent.setPreferredSize(new Dimension(200, 28));
        addComp(pnl, cboStudent, g, 1, row++);

        addLabel(pnl, "Lớp học:", g, 0, row);
        cboClass = new JComboBox<>();
        cboClass.setPreferredSize(new Dimension(200, 28));
        addComp(pnl, cboClass, g, 1, row++);

        addLabel(pnl, "Khuyến mãi:", g, 0, row);
        cboPromotion = new JComboBox<>();
        cboPromotion.setPreferredSize(new Dimension(200, 28));
        addComp(pnl, cboPromotion, g, 1, row++);

        addLabel(pnl, "Học phí gốc:", g, 2, 0);
        lblOriginalFee = makeValueLabel("0 VND");
        addComp(pnl, lblOriginalFee, g, 3, 0);

        addLabel(pnl, "Giảm giá:", g, 2, 1);
        lblDiscount = makeValueLabel("0 VND");
        lblDiscount.setForeground(new Color(178, 34, 34));
        addComp(pnl, lblDiscount, g, 3, 1);

        addLabel(pnl, "Tổng tiền:", g, 2, 2);
        lblTotal = makeValueLabel("0 VND");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(0, 128, 0));
        addComp(pnl, lblTotal, g, 3, 2);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnCreateInvoice = makeButton("🧾 Tạo Hóa Đơn", new Color(218, 165, 32));
        JButton btnRefresh = makeButton("🔄 Làm Mới", new Color(128, 128, 128));
        btnRow.add(btnCreateInvoice);
        btnRow.add(btnRefresh);
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 4;
        pnl.add(btnRow, g);

        cboClass.addActionListener(e -> recalcPreview());
        cboPromotion.addActionListener(e -> recalcPreview());
        btnCreateInvoice.addActionListener(e -> handleCreateInvoice());
        btnRefresh.addActionListener(e -> refreshComboData());
        return pnl;
    }

    private JPanel buildInvoiceTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setOpaque(false);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        bar.setOpaque(false);
        JComboBox<String> cboFilter = new JComboBox<>(
                new String[] { "Tất cả", "Issued", "Paid", "Draft", "Cancelled" });
        JButton btnFilter = makeButton("Lọc", new Color(70, 130, 180));
        bar.add(new JLabel("Lọc trạng thái:"));
        bar.add(cboFilter);
        bar.add(btnFilter);
        pnl.add(bar, BorderLayout.NORTH);

        String[] cols = { "ID", "Học Viên", "Tổng Tiền (VND)", "Ngày Lập", "Trạng Thái", "Ghi Chú" };
        invoiceModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblInvoice = new JTable(invoiceModel);
        styleTable(tblInvoice);
        tblInvoice.getColumnModel().getColumn(0).setMinWidth(0);
        tblInvoice.getColumnModel().getColumn(0).setMaxWidth(0);
        pnl.add(new JScrollPane(tblInvoice), BorderLayout.CENTER);

        tblInvoice.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                refreshPaymentPanel();
        });
        btnFilter.addActionListener(e -> loadInvoiceTable((String) cboFilter.getSelectedItem()));
        return pnl;
    }

    private JPanel buildPaymentPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setPreferredSize(new Dimension(270, 0));
        pnl.setBackground(new Color(240, 255, 240));
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 139, 87), 1),
                "Ghi Nhận Thanh Toán", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), new Color(0, 100, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int row = 0;

        lblInvoiceDetail = new JLabel("<html><i>Chưa chọn hóa đơn</i></html>");
        lblInvoiceDetail.setFont(new Font("Arial", Font.PLAIN, 12));
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        pnl.add(lblInvoiceDetail, g);
        g.gridwidth = 1;

        addLabel(pnl, "Đã thanh toán:", g, 0, row);
        lblPaidSoFar = makeValueLabel("0 VND");
        addComp(pnl, lblPaidSoFar, g, 1, row++);

        addLabel(pnl, "Còn lại:", g, 0, row);
        lblRemaining = makeValueLabel("0 VND");
        lblRemaining.setForeground(new Color(178, 34, 34));
        addComp(pnl, lblRemaining, g, 1, row++);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        pnl.add(new JSeparator(), g);
        g.gridwidth = 1;

        addLabel(pnl, "Số tiền (VND):", g, 0, row);
        txtAmount = new JTextField("0");
        styleField(txtAmount);
        addComp(pnl, txtAmount, g, 1, row++);

        addLabel(pnl, "Phương thức:", g, 0, row);
        cboMethod = new JComboBox<>(
                new String[] { "Cash", "Bank", "Momo", "ZaloPay", "Card", "Other" });
        styleCombo(cboMethod);
        addComp(pnl, cboMethod, g, 1, row++);

        addLabel(pnl, "Mã tham chiếu:", g, 0, row);
        txtRef = new JTextField();
        styleField(txtRef);
        addComp(pnl, txtRef, g, 1, row++);

        btnPay = makeButton("💳 Xác Nhận Thanh Toán", new Color(46, 139, 87));
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        pnl.add(btnPay, g);

        btnMarkPaid = makeButton("✅ Đánh Dấu Đã Thanh Toán Đủ", new Color(25, 25, 112));
        g.gridy = row++;
        pnl.add(btnMarkPaid, g);

        btnPay.addActionListener(e -> handlePayment());
        btnMarkPaid.addActionListener(e -> handleMarkPaid());
        return pnl;
    }

    // ── Business logic ────────────────────────────────────────────────────────
    private void recalcPreview() {
        Class_ c = (Class_) cboClass.getSelectedItem();
        Promotion p = (Promotion) cboPromotion.getSelectedItem();
        if (c == null || c.getCourse() == null) {
            lblOriginalFee.setText("0 VND");
            lblDiscount.setText("0 VND");
            lblTotal.setText("0 VND");
            return;
        }
        BigDecimal fee = c.getCourse().getFee();
        BigDecimal total = invoiceService.calculateTotal(fee, p);
        lblOriginalFee.setText(formatVND(fee));
        lblDiscount.setText("-" + formatVND(fee.subtract(total)));
        lblTotal.setText(formatVND(total));
    }

    private void handleCreateInvoice() {
        Student s = (Student) cboStudent.getSelectedItem();
        Class_ c = (Class_) cboClass.getSelectedItem();
        if (s == null || c == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn Học viên và Lớp học.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Promotion p = (Promotion) cboPromotion.getSelectedItem();
            Invoice inv = invoiceService.createInvoice(s, c, p);
            JOptionPane.showMessageDialog(this,
                    "Tạo hóa đơn thành công!\nTổng tiền: " + formatVND(inv.getTotalAmount()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadInvoiceTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePayment() {
        int row = tblInvoice.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần thanh toán.",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long invoiceId = (Long) invoiceModel.getValueAt(row, 0);
        invoiceService.findById(invoiceId).ifPresent(inv -> {
            try {
                BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
                Payment payment = new Payment();
                payment.setStudent(inv.getStudent());
                payment.setInvoice(inv);
                payment.setAmount(amount);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentMethod(
                        Payment.PaymentMethod.valueOf((String) cboMethod.getSelectedItem()));
                payment.setStatus(Payment.PaymentStatus.Completed);
                String ref = txtRef.getText().trim();
                payment.setReferenceCode(ref.isEmpty() ? null : ref);
                paymentRepo.save(payment);
                JOptionPane.showMessageDialog(this,
                        "Đã ghi nhận thanh toán " + formatVND(amount) + ".",
                        "Thanh toán", JOptionPane.INFORMATION_MESSAGE);
                loadInvoiceTable();
                refreshPaymentPanel();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Số tiền không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void handleMarkPaid() {
        int row = tblInvoice.getSelectedRow();
        if (row < 0)
            return;
        Long invoiceId = (Long) invoiceModel.getValueAt(row, 0);
        try {
            invoiceService.markAsPaid(invoiceId);
            loadInvoiceTable();
            JOptionPane.showMessageDialog(this,
                    "Đã đánh dấu thanh toán đủ.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Chưa đủ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInvoiceTable() {
        loadInvoiceTable("Tất cả");
    }

    private void loadInvoiceTable(String statusFilter) {
        invoiceModel.setRowCount(0);
        try {
            invoiceService.findAll().stream()
                    .filter(i -> "Tất cả".equals(statusFilter)
                            || i.getStatus().name().equals(statusFilter))
                    .forEach(i -> invoiceModel.addRow(new Object[] {
                            i.getInvoiceId(), i.getStudent().getFullName(),
                            formatVND(i.getTotalAmount()), i.getIssueDate(),
                            i.getStatus(), i.getNote()
                    }));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi load hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPaymentPanel() {
        int row = tblInvoice.getSelectedRow();
        if (row < 0) {
            lblInvoiceDetail.setText("<html><i>Chưa chọn hóa đơn</i></html>");
            lblPaidSoFar.setText("0 VND");
            lblRemaining.setText("0 VND");
            return;
        }
        Long invoiceId = (Long) invoiceModel.getValueAt(row, 0);
        String hv = (String) invoiceModel.getValueAt(row, 1);
        String total = (String) invoiceModel.getValueAt(row, 2);
        lblInvoiceDetail.setText("<html><b>" + hv + "</b><br/>Tổng: " + total + "</html>");
        invoiceService.findById(invoiceId).ifPresent(inv -> {
            BigDecimal paid = paymentRepo.sumAmountByInvoice(inv);
            BigDecimal rem = inv.getTotalAmount().subtract(paid).max(BigDecimal.ZERO);
            lblPaidSoFar.setText(formatVND(paid));
            lblRemaining.setText(formatVND(rem));
        });
    }

    private void loadPromotions() {
        cboPromotion.removeAllItems();
        cboPromotion.addItem(null);
        try {
            promotionRepo.findAllActive().forEach(cboPromotion::addItem);
        } catch (Exception ignored) {
        }
    }

    /** Setters để cha override nếu cần */
    public void setStudents(List<Student> students) {
        cboStudent.removeAllItems();
        students.forEach(cboStudent::addItem);
    }

    public void setClasses(List<Class_> classes) {
        cboClass.removeAllItems();
        classes.forEach(cboClass::addItem);
        recalcPreview();
    }

    private String formatVND(BigDecimal val) {
        return val == null ? "0 VND" : String.format("%,.0f VND", val);
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(24);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        final Color HDR_BG = new Color(218, 165, 32);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setBackground(HDR_BG);
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(160, 120, 10)));
                setOpaque(true);
                return this;
            }
        });
        t.setSelectionBackground(new Color(255, 239, 186));
    }

    private static void addLabel(JPanel p, String text, GridBagConstraints g, int x, int y) {
        g.gridx = x;
        g.gridy = y;
        g.weightx = 0;
        p.add(new JLabel(text), g);
    }

    private static void addComp(JPanel p, Component c, GridBagConstraints g, int x, int y) {
        g.gridx = x;
        g.gridy = y;
        g.weightx = 1;
        p.add(c, g);
    }

    private static JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    private static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private static void styleField(JTextField f) {
        f.setBackground(Color.WHITE);
        f.setForeground(new Color(30, 30, 30));
        f.setCaretColor(new Color(218, 165, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 120), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(Color.WHITE);
        cb.setForeground(new Color(30, 30, 30));
        cb.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 120), 1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Preview – InvoicePaymentPanel");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 650);
            f.add(new InvoicePaymentPanel());
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
