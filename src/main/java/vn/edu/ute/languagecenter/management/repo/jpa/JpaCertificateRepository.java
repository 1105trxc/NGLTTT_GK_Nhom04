package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.Certificate;
import vn.edu.ute.languagecenter.management.repo.CertificateRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai CertificateRepository bằng JPA/Hibernate.
 */
public class JpaCertificateRepository implements CertificateRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public Certificate save(Certificate cert) {
        try {
            return txm.runInTransaction(em -> {
                em.persist(cert);
                return cert;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Certificate update(Certificate cert) {
        try {
            return txm.runInTransaction(em -> em.merge(cert));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Certificate> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(Certificate.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Certificate> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT c FROM Certificate c", Certificate.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                Certificate c = em.find(Certificate.class, id);
                if (c != null)
                    em.remove(c);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Certificate> findByStudentId(Long studentId) {
        EntityManager em = Jpa.em();
        try {
            // Sắp xếp theo ngày cấp mới nhất trước
            return em.createQuery(
                    "SELECT c FROM Certificate c WHERE c.student.studentId = :sid ORDER BY c.issueDate DESC",
                    Certificate.class)
                    .setParameter("sid", studentId).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Certificate> findByClassId(Long classId) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Certificate c WHERE c.class_.classId = :cid", Certificate.class)
                    .setParameter("cid", classId).getResultList();
        } finally {
            em.close();
        }
    }
}
