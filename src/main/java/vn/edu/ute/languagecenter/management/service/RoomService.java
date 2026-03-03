package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.repo.RoomRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaRoomRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ Phòng học.
 */
public class RoomService {

    private final RoomRepository roomRepo = new JpaRoomRepository();

    public Room save(Room room) {
        validate(room);
        return roomRepo.save(room);
    }

    public Room update(Room room) {
        validate(room);
        return roomRepo.update(room);
    }

    public Optional<Room> findById(Long id) {
        return roomRepo.findById(id);
    }

    public List<Room> findAll() {
        return roomRepo.findAll();
    }

    public List<Room> findAllActive() {
        return roomRepo.findAllActive();
    }

    public List<Room> findByName(String keyword) {
        return roomRepo.findByName(keyword);
    }

    public void deleteById(Long id) {
        roomRepo.deleteById(id);
    }

    /**
     * [LAMBDA 3] Sắp xếp phòng theo sức chứa giảm dần.
     * Dùng Comparator lambda: (Room r) -> r.getCapacity()
     * .reversed() đảo thành giảm dần.
     */
    public List<Room> findAllSortedByCapacity() {
        return roomRepo.findAll().stream()
                .sorted(Comparator.comparingInt(
                        (Room r) -> r.getCapacity() != null ? r.getCapacity() : 0).reversed())
                .toList();
    }

    // ---- Kiểm tra dữ liệu ----
    private void validate(Room room) {
        if (room.getRoomName() == null || room.getRoomName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Sức chứa phải lớn hơn 0.");
        }
    }
}
