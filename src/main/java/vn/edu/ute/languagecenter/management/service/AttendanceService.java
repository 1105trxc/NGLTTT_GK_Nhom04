package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Attendance;
import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.AttendanceRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaAttendanceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceService {

    private final AttendanceRepository attendanceRepo = new JpaAttendanceRepository();

    /**
     * Lưu toàn bộ điểm danh cho một buổi học.
     * Stream pipeline: load existing → collect thành Map<studentId, Attendance>
     *   → forEach statusMap để update/tạo mới → saveAll.
     * Tránh vi phạm UNIQUE(student_id, class_id, attend_date).
     */
    public List<Attendance> saveSession(Class_ class_, LocalDate date,
            Map<Student, Attendance.AttendanceStatus> statusMap,
            Map<Student, String> noteMap) {

        // Pipeline 1: load existing records, index bằng studentId để lookup O(1)
        Map<Long, Attendance> existingMap = attendanceRepo
            .findByClassAndDate(class_, date).stream()
            .collect(Collectors.toMap(
                a -> a.getStudent().getStudentId(),
                a -> a
            ));

        List<Attendance> toSave = new ArrayList<>();
        statusMap.forEach((student, status) -> {
            Attendance att = existingMap.getOrDefault(
                student.getStudentId(), new Attendance());
            att.setStudent(student);
            att.setClass_(class_);
            att.setAttendDate(date);
            att.setStatus(status);
            att.setNote(noteMap != null ? noteMap.get(student) : null);
            toSave.add(att);
        });

        return attendanceRepo.saveAll(toSave);
    }

    /** Load danh sách điểm danh buổi học → prefill form giáo viên */
    public List<Attendance> getSession(Class_ class_, LocalDate date) {
        return attendanceRepo.findByClassAndDate(class_, date);
    }

    public List<Attendance> getByStudent(Student student) {
        return attendanceRepo.findByStudent(student);
    }

    public long countAbsent(Student student, Class_ class_) {
        return attendanceRepo.countAbsentByStudentAndClass(student, class_);
    }

    /**
     * Lấy danh sách học viên vắng mặt trong một buổi học.
     * Stream pipeline: filter Absent → map sang Student → sort tên → collect.
     */
    public List<Student> getAbsentStudents(Class_ class_, LocalDate date) {
        return attendanceRepo.findByClassAndDate(class_, date).stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.Absent)
            .map(Attendance::getStudent)
            .sorted((s1, s2) -> s1.getFullName().compareTo(s2.getFullName()))
            .collect(Collectors.toList());
    }
}
