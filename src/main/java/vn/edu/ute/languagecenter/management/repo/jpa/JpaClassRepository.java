package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.repo.ClassRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai ClassRepository bằng JPA/Hibernate.
 * Hỗ trợ lọc theo khóa học, giáo viên, trạng thái và đếm enrolled.
 */
public class JpaClassRepository implements ClassRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public Class_ save(Class_ class_) {
        try {
            return txm.runInTransaction(em -> {
                em.persist(class_);
                return class_;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class_ update(Class_ class_) {
        try {
            return txm.runInTransaction(em -> em.merge(class_));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Class_> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(Class_.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Class_> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT c FROM Class_ c", Class_.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                Class_ c = em.find(Class_.class, id);
                if (c != null)
                    em.remove(c);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class_> findByCourseId(Long courseId) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.course.courseId = :cid", Class_.class)
                    .setParameter("cid", courseId).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Class_> findByTeacherId(Long teacherId) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.teacher.teacherId = :tid", Class_.class)
                    .setParameter("tid", teacherId).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Class_> findByStatus(Class_.ClassStatus status) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.status = :st", Class_.class)
                    .setParameter("st", status).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Class_> findByName(String keyword) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE LOWER(c.className) LIKE LOWER(:kw)", Class_.class)
                    .setParameter("kw", "%" + keyword + "%").getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countEnrolledStudents(Long classId) {
        EntityManager em = Jpa.em();
        try {
            // Đếm số enrollment có status='Enrolled' thuộc lớp này
            return em.createQuery(
                    "SELECT COUNT(e) FROM Enrollment e WHERE e.class_.classId = :cid AND e.status = 'Enrolled'",
                    Long.class)
                    .setParameter("cid", classId).getSingleResult();
        } finally {
            em.close();
        }
    }
}
