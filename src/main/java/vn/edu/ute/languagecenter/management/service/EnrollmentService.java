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
        // 1. Kiểm tra trạng thái lớp có cho phép ghi danh không
        if (class_.getStatus() == Class_.ClassStatus.Completed ||
                class_.getStatus() == Class_.ClassStatus.Cancelled) {
            throw new IllegalStateException(
                    "Lớp '" + class_.getClassName() + "' không còn nhận ghi danh.");
        }

        // 2. Kiểm tra sĩ số lớp có đầy chưa
        long currentEnrolled = enrollmentRepo.countEnrolledByClass(class_);
        if (class_.getMaxStudent() > 0 && currentEnrolled >= class_.getMaxStudent()) {
            throw new IllegalStateException(
                    "Lớp '" + class_.getClassName() + "' đã đủ sĩ số (" + class_.getMaxStudent() + ").");
        }

        // 3. Tìm xem học viên đã từng có bản ghi (record) trong lớp này chưa bằng Stream
        Optional<Enrollment> existingOpt = enrollmentRepo.findByClass(class_).stream()
                .filter(e -> e.getStudent().getStudentId().equals(student.getStudentId()))
                .findFirst();

        // NẾU ĐÃ TỪNG CÓ DỮ LIỆU TRONG LỚP NÀY
        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();

            // Nếu đang học -> Báo lỗi
            if (existing.getStatus() == Enrollment.EnrollmentStatus.Enrolled) {
                throw new IllegalStateException(
                        "Học viên '" + student.getFullName() + "' đang theo học lớp này rồi.");
            }

            // Nếu đã hoàn thành -> Báo lỗi (không cho học lại lớp đã đậu/kết thúc)
            if (existing.getStatus() == Enrollment.EnrollmentStatus.Completed) {
                throw new IllegalStateException(
                        "Học viên '" + student.getFullName() + "' đã hoàn thành lớp này.");
            }

            // Nếu từng bị rớt/nghỉ (Dropped) -> Reset lại trạng thái để tiếp tục học
            if (existing.getStatus() == Enrollment.EnrollmentStatus.Dropped) {
                existing.setStatus(Enrollment.EnrollmentStatus.Enrolled);
                existing.setEnrollmentDate(LocalDate.now()); // Cập nhật lại ngày ghi danh mới
                existing.setResult(Enrollment.ResultStatus.NA); // Reset kết quả về NA
                return enrollmentRepo.save(existing);
            }
        }

        // 4. NẾU LÀ HỌC VIÊN HOÀN TOÀN MỚI CHƯA TỪNG HỌC LỚP NÀY
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
