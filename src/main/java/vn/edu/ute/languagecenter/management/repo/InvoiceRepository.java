package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Student;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    List<Invoice> findAll();
    List<Invoice> findByStudent(Student student);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    void updateStatus(Long id, Invoice.InvoiceStatus status);
    void deleteById(Long id);
}
