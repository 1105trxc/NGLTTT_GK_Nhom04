package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findById(Long id);
    List<Enrollment> findAll();
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByClass(Class_ class_);
    boolean existsByStudentAndClass(Student student, Class_ class_);
    long countEnrolledByClass(Class_ class_);
    void deleteById(Long id);
    void updateStatus(Long id, Enrollment.EnrollmentStatus status);
}
