package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.dao.RoomDAO;
import vn.edu.ute.languagecenter.management.model.Room;

import java.util.List;
import java.util.Optional;

public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();

    public Room save(Room room) {
        validate(room);
        return roomDAO.save(room);
    }

    public Room update(Room room) {
        validate(room);
        return roomDAO.update(room);
    }

    public Optional<Room> findById(Long id) {
        return roomDAO.findById(id);
    }

    public List<Room> findAll() {
        return roomDAO.findAll();
    }

    public List<Room> findAllActive() {
        return roomDAO.findAllActive();
    }

    public List<Room> findByName(String keyword) {
        return roomDAO.findByName(keyword);
    }

    public void deleteById(Long id) {
        roomDAO.deleteById(id);
    }

    // ---- Validation ----
    private void validate(Room room) {
        if (room.getRoomName() == null || room.getRoomName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Sức chứa phải lớn hơn 0.");
        }
    }
}
