package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.PlacementTest;
import vn.edu.ute.languagecenter.management.repo.PlacementTestRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai PlacementTestRepository bằng JPA/Hibernate.
 */
public class JpaPlacementTestRepository implements PlacementTestRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public PlacementTest save(PlacementTest test) {
        try {
            return txm.runInTransaction(em -> {
                em.persist(test);
                return test;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlacementTest update(PlacementTest test) {
        try {
            return txm.runInTransaction(em -> em.merge(test));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<PlacementTest> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(PlacementTest.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<PlacementTest> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT p FROM PlacementTest p", PlacementTest.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                PlacementTest t = em.find(PlacementTest.class, id);
                if (t != null)
                    em.remove(t);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PlacementTest> findByStudentId(Long studentId) {
        EntityManager em = Jpa.em();
        try {
            // Sắp xếp theo ngày thi mới nhất trước
            return em.createQuery(
                    "SELECT p FROM PlacementTest p WHERE p.student.studentId = :sid ORDER BY p.testDate DESC",
                    PlacementTest.class)
                    .setParameter("sid", studentId).getResultList();
        } finally {
            em.close();
        }
    }
}
