package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Invoice;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.InvoiceRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaInvoiceRepository implements InvoiceRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Invoice save(Invoice invoice) {
        try {
            return tm.runInTransaction(em -> {
                if (invoice.getInvoiceId() == null) {
                    em.persist(invoice);
                    return invoice;
                }
                return em.merge(invoice);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Invoice", e);
        }
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                    Optional.ofNullable(em.find(Invoice.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Invoice", e);
        }
    }

    @Override
    public List<Invoice> findAll() {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT i FROM Invoice i JOIN FETCH i.student ORDER BY i.issueDate DESC",
                            Invoice.class
                    ).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findAll Invoice", e);
        }
    }

    @Override
    public List<Invoice> findByStudent(Student student) {
        return findAll().stream()
                .filter(i -> i.getStudent().getStudentId()
                        .equals(student.getStudentId()))
                .sorted((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByStatus(Invoice.InvoiceStatus status) {
        return findAll().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long id, Invoice.InvoiceStatus status) {
        try {
            tm.runInTransaction(em -> {
                em.createQuery(
                                "UPDATE Invoice i SET i.status = :s WHERE i.invoiceId = :id"
                        ).setParameter("s", status)
                        .setParameter("id", id)
                        .executeUpdate();
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi updateStatus Invoice", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Invoice inv = em.find(Invoice.class, id);
                if (inv != null) em.remove(inv);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Invoice", e);
        }
    }
}