package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Payment;
import vn.edu.ute.languagecenter.management.model.Student;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    List<Payment> findAll();
    List<Payment> findByStudent(Student student);
    List<Payment> findByInvoice(Invoice invoice);
    List<Payment> findByEnrollment(Enrollment enrollment);
    BigDecimal sumAmountByInvoice(Invoice invoice);
    void deleteById(Long id);
}
