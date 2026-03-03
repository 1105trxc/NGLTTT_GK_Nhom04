package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.Course;
import vn.edu.ute.languagecenter.management.repo.CourseRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai CourseRepository bằng JPA/Hibernate.
 * - Thao tác ghi (save/update/delete) dùng TransactionManager + lambda
 * - Thao tác đọc (find) dùng EntityManager trực tiếp
 */
public class JpaCourseRepository implements CourseRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public Course save(Course course) {
        try {
            // Lambda: truyền logic persist vào TransactionManager
            return txm.runInTransaction(em -> {
                em.persist(course);
                return course;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Course update(Course course) {
        try {
            // merge() cập nhật entity đã detach về DB
            return txm.runInTransaction(em -> em.merge(course));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Course> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(Course.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Course> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT c FROM Course c", Course.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                Course c = em.find(Course.class, id);
                if (c != null)
                    em.remove(c); // Chỉ xóa nếu tồn tại
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Course> findByName(String keyword) {
        EntityManager em = Jpa.em();
        try {
            // LIKE search không phân biệt hoa/thường
            return em.createQuery(
                    "SELECT c FROM Course c WHERE LOWER(c.courseName) LIKE LOWER(:kw)", Course.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Course> findAllActive() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT c FROM Course c WHERE c.status = :st", Course.class)
                    .setParameter("st", Course.ActiveStatus.Active)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
