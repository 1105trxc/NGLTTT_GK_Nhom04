package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.model.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    Attendance save(Attendance attendance);
    List<Attendance> saveAll(List<Attendance> list);
    Optional<Attendance> findById(Long id);
    List<Attendance> findByClassAndDate(Class_ class_, LocalDate date);
    List<Attendance> findByStudent(Student student);
    List<Attendance> findByClass(Class_ class_);
    long countAbsentByStudentAndClass(Student student, Class_ class_);
    void deleteById(Long id);
}
