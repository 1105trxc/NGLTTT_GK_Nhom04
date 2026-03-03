package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Payment;
import vn.edu.ute.languagecenter.management.model.Promotion;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.InvoiceRepository;
import vn.edu.ute.languagecenter.management.repo.PaymentRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaInvoiceRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaPaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceService {

    private final InvoiceRepository  invoiceRepo  = new JpaInvoiceRepository();
    private final PaymentRepository  paymentRepo  = new JpaPaymentRepository();

    /**
     * Tạo hóa đơn khi học viên ghi danh.
     * Công thức: tiền gốc (Course.fee) - giảm giá (Promotion) = totalAmount
     */
    public Invoice createInvoice(Student student, Class_ class_, Promotion promotion) {
        BigDecimal total = calculateTotal(class_.getCourse().getFee(), promotion);

        Invoice invoice = new Invoice();
        invoice.setStudent(student);
        invoice.setTotalAmount(total);
        invoice.setIssueDate(LocalDate.now());
        invoice.setStatus(Invoice.InvoiceStatus.Issued);
        invoice.setPromotion(promotion);
        invoice.setNote("Học phí lớp " + class_.getClassName());
        return invoiceRepo.save(invoice);
    }

    /**
     * Tính tổng tiền sau khi áp dụng promotion — dùng để preview trên form.
     * Stream pipeline: nếu promotion null → trả nguyên fee;
     *   nếu Percent → fee * value / 100; nếu Amount → fee - value.
     */
    public BigDecimal calculateTotal(BigDecimal fee, Promotion promotion) {
        if (promotion == null) return fee;

        BigDecimal discount = (promotion.getDiscountType() == Promotion.DiscountType.Percent)
            ? fee.multiply(promotion.getDiscountValue())
                 .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            : promotion.getDiscountValue();

        BigDecimal total = fee.subtract(discount);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepo.findById(id);
    }

    public List<Invoice> findByStudent(Student student) {
        return invoiceRepo.findByStudent(student);
    }

    public List<Invoice> findAll() {
        return invoiceRepo.findAll();
    }

    /**
     * Đánh dấu Paid — chỉ cho phép nếu tổng Payment đã đủ totalAmount.
     */
    public void markAsPaid(Long invoiceId) {
        invoiceRepo.findById(invoiceId).ifPresent(inv -> {
            BigDecimal paid = paymentRepo.sumAmountByInvoice(inv);
            if (paid.compareTo(inv.getTotalAmount()) >= 0) {
                invoiceRepo.updateStatus(invoiceId, Invoice.InvoiceStatus.Paid);
            } else {
                throw new IllegalStateException(
                    "Chưa thanh toán đủ. Còn thiếu: "
                    + inv.getTotalAmount().subtract(paid).toPlainString() + " VND");
            }
        });
    }

    public void cancelInvoice(Long invoiceId) {
        invoiceRepo.updateStatus(invoiceId, Invoice.InvoiceStatus.Cancelled);
    }
}
