package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho Schedule (Lịch học).
 * Bao gồm logic kiểm tra trùng lịch phòng — phần quan trọng nhất.
 */
public interface ScheduleRepository {
    Schedule save(Schedule schedule); // Thêm mới

    Schedule update(Schedule schedule); // Cập nhật

    Optional<Schedule> findById(Long id); // Tìm theo ID

    List<Schedule> findAll(); // Lấy tất cả

    List<Schedule> findByTeacherId(Long teacherId); // Lọc lịch theo giáo viên

    void deleteById(Long id); // Xóa theo ID

    List<Schedule> findByClassId(Long classId); // Lọc lịch theo lớp

    List<Schedule> findByRoomAndDate(Long roomId, LocalDate date); // Lọc theo phòng + ngày

    /**
     * Kiểm tra trùng lịch phòng khi THÊM MỚI.
     * Hai khoảng thời gian giao nhau khi: start1 < end2 AND start2 < end1
     */
    boolean isRoomConflict(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Kiểm tra trùng lịch phòng khi CẬP NHẬT — loại trừ schedule hiện tại.
     */
    boolean isRoomConflictExcluding(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
            Long excludeScheduleId);

    /** Lấy lịch theo khoảng ngày (tuần/tháng) */
    List<Schedule> findByDateRange(LocalDate from, LocalDate to);
}
