package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho Room.
 * Định nghĩa các phương thức truy xuất dữ liệu phòng học.
 */
public interface RoomRepository {
    Room save(Room room); // Thêm mới

    Room update(Room room); // Cập nhật

    Optional<Room> findById(Long id); // Tìm theo ID

    List<Room> findAll(); // Lấy tất cả

    void deleteById(Long id); // Xóa theo ID

    List<Room> findByName(String keyword); // Tìm theo tên (LIKE)

    List<Room> findAllActive(); // Lấy phòng đang Active
}
