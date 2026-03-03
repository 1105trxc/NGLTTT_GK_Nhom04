package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho Course.
 * Định nghĩa các phương thức truy xuất dữ liệu khóa học,
 * tách biệt logic nghiệp vụ (Service) khỏi chi tiết truy vấn (JPA).
 */
public interface CourseRepository {
    Course save(Course course); // Thêm mới

    Course update(Course course); // Cập nhật

    Optional<Course> findById(Long id); // Tìm theo ID

    List<Course> findAll(); // Lấy tất cả

    void deleteById(Long id); // Xóa theo ID

    List<Course> findByName(String keyword); // Tìm theo tên (LIKE)

    List<Course> findAllActive(); // Lấy khóa học đang Active
}
