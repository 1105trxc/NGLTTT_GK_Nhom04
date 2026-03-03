package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.PlacementTest;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho PlacementTest (Bài kiểm tra đầu vào).
 */
public interface PlacementTestRepository {
    PlacementTest save(PlacementTest test); // Lưu kết quả test

    PlacementTest update(PlacementTest test); // Cập nhật

    Optional<PlacementTest> findById(Long id); // Tìm theo ID

    List<PlacementTest> findAll(); // Lấy tất cả

    void deleteById(Long id); // Xóa

    List<PlacementTest> findByStudentId(Long studentId); // Lọc theo học viên
}
