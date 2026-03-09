package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.EnrollmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaEnrollmentRepository implements EnrollmentRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Enrollment save(Enrollment enrollment) {
        try {
            return tm.runInTransaction(em -> {
                if (enrollment.getEnrollmentId() == null) {
                    em.persist(enrollment);
                    return enrollment;
                }
                return em.merge(enrollment);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Enrollment", e);
        }
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                Optional.ofNullable(em.find(Enrollment.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Enrollment", e);
        }
    }

    @Override
    public List<Enrollment> findAll() {
        try {
            return tm.runInTransaction(em ->
                em.createQuery(
                    "SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.class_",
                    Enrollment.class
                ).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findAll Enrollment", e);
        }
    }

    @Override
    public List<Enrollment> findByStudent(Student student) {
        try {
            List<Enrollment> all = findAll();
            return all.stream()
                .filter(e -> e.getStudent().getStudentId()
                              .equals(student.getStudentId()))
                .sorted((a, b) -> b.getEnrollmentDate()
                                   .compareTo(a.getEnrollmentDate()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByStudent", e);
        }
    }

    @Override
    public List<Enrollment> findByClass(Class_ class_) {
        try {
            return tm.runInTransaction(em ->
                em.createQuery(
                    "SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.class_ " +
                    "WHERE e.class_ = :c ORDER BY e.student.fullName",
                    Enrollment.class
                ).setParameter("c", class_).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByClass", e);
        }
    }

    @Override
    public boolean existsByStudentAndClass(Student student, Class_ class_) {
        return findAll().stream()
            .anyMatch(e ->
                e.getStudent().getStudentId().equals(student.getStudentId()) &&
                e.getClass_().getClassId().equals(class_.getClassId())
            );
    }

    @Override
    public long countEnrolledByClass(Class_ class_) {
        return findByClass(class_).stream()
            .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.Enrolled)
            .count();
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Enrollment e = em.find(Enrollment.class, id);
                if (e != null) em.remove(e);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Enrollment", e);
        }
    }

    @Override
    public void updateStatus(Long id, Enrollment.EnrollmentStatus status) {
        try {
            tm.runInTransaction(em -> {
                em.createQuery(
                    "UPDATE Enrollment e SET e.status = :s WHERE e.enrollmentId = :id"
                ).setParameter("s", status).setParameter("id", id).executeUpdate();
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi updateStatus Enrollment", e);
        }
    }
}
