package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.dao.ScheduleDAO;
import vn.edu.ute.languagecenter.management.model.Schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class ScheduleService {

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    /**
     * Lưu lịch học mới — kiểm tra trùng lịch phòng trước khi lưu.
     */
    public Schedule save(Schedule schedule) {
        validate(schedule);
        checkRoomAvailability(schedule.getRoom().getRoomId(),
                schedule.getStudyDate(), schedule.getStartTime(), schedule.getEndTime(), null);
        return scheduleDAO.save(schedule);
    }

    /**
     * Cập nhật lịch học — kiểm tra trùng lịch (loại trừ chính nó).
     */
    public Schedule update(Schedule schedule) {
        validate(schedule);
        checkRoomAvailability(schedule.getRoom().getRoomId(),
                schedule.getStudyDate(), schedule.getStartTime(), schedule.getEndTime(),
                schedule.getScheduleId());
        return scheduleDAO.update(schedule);
    }

    public Optional<Schedule> findById(Long id) {
        return scheduleDAO.findById(id);
    }

    public List<Schedule> findAll() {
        return scheduleDAO.findAll();
    }

    public List<Schedule> findByClassId(Long classId) {
        return scheduleDAO.findByClassId(classId);
    }

    public List<Schedule> findByRoomAndDate(Long roomId, LocalDate date) {
        return scheduleDAO.findByRoomAndDate(roomId, date);
    }

    public List<Schedule> findByDateRange(LocalDate from, LocalDate to) {
        return scheduleDAO.findByDateRange(from, to);
    }

    public void deleteById(Long id) {
        scheduleDAO.deleteById(id);
    }

    // ---- LOGIC QUAN TRỌNG: Kiểm tra trùng lịch phòng ----
    /**
     * Kiểm tra phòng có còn trống trong khung giờ chỉ định hay không.
     * 
     * @param excludeId ID lịch hiện tại cần loại trừ (khi update), null khi tạo
     *                  mới.
     * @throws IllegalArgumentException nếu phòng đã bị trùng.
     */
    private void checkRoomAvailability(Long roomId, LocalDate date, LocalTime startTime,
            LocalTime endTime, Long excludeId) {
        boolean conflict;
        if (excludeId == null) {
            conflict = scheduleDAO.isRoomConflict(roomId, date, startTime, endTime);
        } else {
            conflict = scheduleDAO.isRoomConflictExcluding(roomId, date, startTime, endTime, excludeId);
        }
        if (conflict) {
            throw new IllegalArgumentException(
                    "Phòng đã có lịch trùng vào ngày " + date + " (" + startTime + " - " + endTime
                            + "). Vui lòng chọn phòng hoặc khung giờ khác.");
        }
    }

    // ---- Validation cơ bản ----
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
