package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Schedule;
import vn.edu.ute.languagecenter.management.repo.ScheduleRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ Lịch học.
 * Bao gồm logic quan trọng: kiểm tra trùng lịch phòng trước khi lưu.
 */
public class ScheduleService {

    private final ScheduleRepository scheduleRepo = new JpaScheduleRepository();

    /**
     * Thêm lịch mới — kiểm tra trùng phòng trước khi lưu.
     * excludeId = null vì đây là bản ghi mới, không cần loại trừ.
     */
    public Schedule save(Schedule schedule) {
        validate(schedule);
        checkRoomAvailability(schedule.getRoom().getRoomId(),
                schedule.getStudyDate(), schedule.getStartTime(), schedule.getEndTime(), null);
        return scheduleRepo.save(schedule);
    }

    /**
     * Cập nhật lịch — loại trừ chính nó khi kiểm tra trùng.
     */
    public Schedule update(Schedule schedule) {
        validate(schedule);
        checkRoomAvailability(schedule.getRoom().getRoomId(),
                schedule.getStudyDate(), schedule.getStartTime(), schedule.getEndTime(),
                schedule.getScheduleId());
        return scheduleRepo.update(schedule);
    }

    public Optional<Schedule> findById(Long id) {
        return scheduleRepo.findById(id);
    }

    public List<Schedule> findByTeacherId(Long teacherId) {
        return scheduleRepo.findByTeacherId(teacherId);
    }

    public List<Schedule> findAll() {
        return scheduleRepo.findAll();
    }

    public List<Schedule> findByClassId(Long classId) {
        return scheduleRepo.findByClassId(classId);
    }

    public List<Schedule> findByRoomAndDate(Long roomId, LocalDate date) {
        return scheduleRepo.findByRoomAndDate(roomId, date);
    }

    public List<Schedule> findByDateRange(LocalDate from, LocalDate to) {
        return scheduleRepo.findByDateRange(from, to);
    }

    public void deleteById(Long id) {
        scheduleRepo.deleteById(id);
    }

    /**
     * [LAMBDA 5] Lọc lịch theo ngày cụ thể bằng stream + filter lambda.
     * Lambda: s -> date.equals(s.getStudyDate()) so sánh ngày học.
     */
    public List<Schedule> findByExactDate(LocalDate date) {
        return scheduleRepo.findAll().stream()
                .filter(s -> date.equals(s.getStudyDate()))
                .toList();
    }

    /**
     * Kiểm tra phòng còn trống trong khung giờ chỉ định.
     * 
     * @param excludeId ID schedule cần loại trừ (khi update), null khi thêm mới
     * @throws IllegalArgumentException nếu phòng bị trùng lịch
     */
    private void checkRoomAvailability(Long roomId, LocalDate date, LocalTime startTime,
            LocalTime endTime, Long excludeId) {
        boolean conflict;
        if (excludeId == null) {
            conflict = scheduleRepo.isRoomConflict(roomId, date, startTime, endTime);
        } else {
            conflict = scheduleRepo.isRoomConflictExcluding(roomId, date, startTime, endTime, excludeId);
        }
        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng đã có lịch trùng vào ngày " + date + " (" + startTime + " - " + endTime
                            + "). Vui lòng chọn phòng hoặc khung giờ khác.");
        }
    }

    // ---- Kiểm tra dữ liệu ----
    private void validate(Schedule schedule) {
        if (schedule.getClass_() == null) {
            throw new IllegalArgumentException("Phải chọn lớp học.");
        }
        if (schedule.getStudyDate() == null) {
            throw new IllegalArgumentException("Ngày học không được để trống.");
        }
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không được để trống.");
        }
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
        if (schedule.getRoom() == null) {
            throw new IllegalArgumentException("Phải chọn phòng học.");
        }
    }
}
