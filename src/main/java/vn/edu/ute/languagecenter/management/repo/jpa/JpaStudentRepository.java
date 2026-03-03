package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.StudentRepository;

import java.util.List;

/** JPA triển khai StudentRepository. */
public class JpaStudentRepository extends GenericRepository<Student>
        implements StudentRepository {

    public JpaStudentRepository() {
        super(Student.class);
    }

    /** Lấy tất cả học viên, sắp xếp theo tên A-Z. */
    public List<Student> findAllSorted() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Student s ORDER BY s.fullName ASC", Student.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy học viên đang Active. */
    public List<Student> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Student s WHERE s.status = 'Active' ORDER BY s.fullName ASC", Student.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
