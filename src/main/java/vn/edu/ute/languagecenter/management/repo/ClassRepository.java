package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Class_;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho Class_ (Lớp học).
 * Định nghĩa các phương thức truy xuất và tìm kiếm lớp học.
 */
public interface ClassRepository {
    Class_ save(Class_ class_); // Thêm mới

    Class_ update(Class_ class_); // Cập nhật

    Optional<Class_> findById(Long id); // Tìm theo ID

    List<Class_> findAll(); // Lấy tất cả

    void deleteById(Long id); // Xóa theo ID

    List<Class_> findByCourseId(Long courseId); // Lọc theo khóa học

    List<Class_> findByTeacherId(Long teacherId); // Lọc theo giáo viên

    List<Class_> findByStatus(Class_.ClassStatus status); // Lọc theo trạng thái

    List<Class_> findByName(String keyword); // Tìm theo tên (LIKE)

    long countEnrolledStudents(Long classId); // Đếm số học viên enrolled
}
