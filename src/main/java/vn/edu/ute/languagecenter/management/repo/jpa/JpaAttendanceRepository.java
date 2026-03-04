package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Attendance;
import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.AttendanceRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaAttendanceRepository implements AttendanceRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Attendance save(Attendance attendance) {
        try {
            return tm.runInTransaction(em -> {
                if (attendance.getAttendanceId() == null) {
                    em.persist(attendance);
                    return attendance;
                }
                return em.merge(attendance);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Attendance", e);
        }
    }

    @Override
    public List<Attendance> saveAll(List<Attendance> list) {
        try {
            return tm.runInTransaction(em -> {
                list.forEach(a -> {
                    if (a.getAttendanceId() == null) em.persist(a);
                    else em.merge(a);
                });
                return list;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi saveAll Attendance", e);
        }
    }

    @Override
    public Optional<Attendance> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                    Optional.ofNullable(em.find(Attendance.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Attendance", e);
        }
    }

    @Override
    public List<Attendance> findByClass(Class_ class_) {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT a FROM Attendance a JOIN FETCH a.student " +
                                    "WHERE a.class_ = :c ORDER BY a.attendDate DESC, a.student.fullName",
                            Attendance.class
                    ).setParameter("c", class_).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByClass Attendance", e);
        }
    }

    @Override
    public List<Attendance> findByClassAndDate(Class_ class_, LocalDate date) {
        return findByClass(class_).stream()
                .filter(a -> a.getAttendDate().equals(date))
                .sorted((a, b) -> a.getStudent().getFullName()
                        .compareTo(b.getStudent().getFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Attendance> findByStudent(Student student) {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT a FROM Attendance a WHERE a.student = :s ORDER BY a.attendDate DESC",
                            Attendance.class
                    ).setParameter("s", student).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByStudent Attendance", e);
        }
    }

    @Override
    public long countAbsentByStudentAndClass(Student student, Class_ class_) {
        return findByClass(class_).stream()
                .filter(a -> a.getStudent().getStudentId()
                        .equals(student.getStudentId()))
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.Absent)
                .count();
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Attendance a = em.find(Attendance.class, id);
                if (a != null) em.remove(a);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Attendance", e);
        }
    }
}