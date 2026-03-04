package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Payment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaPaymentRepository implements PaymentRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Payment save(Payment payment) {
        try {
            return tm.runInTransaction(em -> {
                if (payment.getPaymentId() == null) {
                    em.persist(payment);
                    return payment;
                }
                return em.merge(payment);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                    Optional.ofNullable(em.find(Payment.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Payment", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT p FROM Payment p JOIN FETCH p.student ORDER BY p.paymentDate DESC",
                            Payment.class
                    ).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findAll Payment", e);
        }
    }

    @Override
    public List<Payment> findByStudent(Student student) {
        return findAll().stream()
                .filter(p -> p.getStudent().getStudentId()
                        .equals(student.getStudentId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByInvoice(Invoice invoice) {
        return findAll().stream()
                .filter(p -> p.getInvoice() != null &&
                        p.getInvoice().getInvoiceId()
                                .equals(invoice.getInvoiceId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByEnrollment(Enrollment enrollment) {
        return findAll().stream()
                .filter(p -> p.getEnrollment() != null &&
                        p.getEnrollment().getEnrollmentId()
                                .equals(enrollment.getEnrollmentId()))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumAmountByInvoice(Invoice invoice) {
        return findByInvoice(invoice).stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.Completed)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Payment p = em.find(Payment.class, id);
                if (p != null) em.remove(p);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Payment", e);
        }
    }
}