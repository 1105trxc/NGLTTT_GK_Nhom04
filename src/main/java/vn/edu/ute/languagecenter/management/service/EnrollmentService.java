package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Enrollment;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.EnrollmentRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaEnrollmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo = new JpaEnrollmentRepository();

    /**
     * Ghi danh học viên vào lớp.
     * Business rules:
     *   1. Kiểm tra đã ghi danh chưa (existsByStudentAndClass)
     *   2. Kiểm tra sĩ số lớp có đầy chưa (countEnrolledByClass vs maxStudent)
     *   3. Kiểm tra trạng thái lớp có cho phép ghi danh không
     */
    public Enrollment enroll(Student student, Class_ class_) {
        if (enrollmentRepo.existsByStudentAndClass(student, class_)) {
            throw new IllegalStateException(
                "Học viên '" + student.getFullName() + "' đã ghi danh lớp này rồi.");
        }

        long current = enrollmentRepo.countEnrolledByClass(class_);
        if (class_.getMaxStudent() > 0 && current >= class_.getMaxStudent()) {
            throw new IllegalStateException(
                "Lớp '" + class_.getClassName() + "' đã đủ sĩ số (" + class_.getMaxStudent() + ").");
        }

        if (class_.getStatus() == Class_.ClassStatus.Completed ||
            class_.getStatus() == Class_.ClassStatus.Cancelled) {
            throw new IllegalStateException(
                "Lớp '" + class_.getClassName() + "' không còn nhận ghi danh.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClass_(class_);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.Enrolled);
        enrollment.setResult(Enrollment.ResultStatus.NA);
        return enrollmentRepo.save(enrollment);
    }

    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepo.findById(id);
    }

    public List<Enrollment> findByStudent(Student student) {
        return enrollmentRepo.findByStudent(student);
    }

    public List<Enrollment> findByClass(Class_ class_) {
        return enrollmentRepo.findByClass(class_);
    }

    /** Hủy ghi danh → status Dropped */
    public void drop(Long enrollmentId) {
        enrollmentRepo.updateStatus(enrollmentId, Enrollment.EnrollmentStatus.Dropped);
    }

    /** Hoàn thành khóa học → status Completed */
    public void complete(Long enrollmentId) {
        enrollmentRepo.updateStatus(enrollmentId, Enrollment.EnrollmentStatus.Completed);
    }
}
