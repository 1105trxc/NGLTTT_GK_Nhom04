package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.Schedule;
import vn.edu.ute.languagecenter.management.repo.ScheduleRepository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai ScheduleRepository bằng JPA/Hibernate.
 * Bao gồm logic kiểm tra trùng lịch phòng (room conflict).
 */
public class JpaScheduleRepository implements ScheduleRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public Schedule save(Schedule schedule) {
        try {
            return txm.runInTransaction(em -> {
                em.persist(schedule);
                return schedule;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Schedule update(Schedule schedule) {
        try {
            return txm.runInTransaction(em -> em.merge(schedule));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Schedule> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(Schedule.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Schedule> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT s FROM Schedule s", Schedule.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                Schedule s = em.find(Schedule.class, id);
                if (s != null)
                    em.remove(s);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Schedule> findByClassId(Long classId) {
        EntityManager em = Jpa.em();
        try {
            // Sắp xếp theo ngày + giờ bắt đầu
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.class_.classId = :cid ORDER BY s.studyDate, s.startTime",
                    Schedule.class)
                    .setParameter("cid", classId).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Schedule> findByRoomAndDate(Long roomId, LocalDate date) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.room.roomId = :rid AND s.studyDate = :d ORDER BY s.startTime",
                    Schedule.class)
                    .setParameter("rid", roomId).setParameter("d", date).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra trùng lịch phòng khi THÊM MỚI.
     * Logic: 2 khoảng thời gian giao nhau khi start1 < end2 AND start2 < end1
     */
    @Override
    public boolean isRoomConflict(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        EntityManager em = Jpa.em();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Schedule s " +
                            "WHERE s.room.roomId = :rid AND s.studyDate = :d " +
                            "AND s.startTime < :et AND s.endTime > :st",
                    Long.class)
                    .setParameter("rid", roomId).setParameter("d", date)
                    .setParameter("st", startTime).setParameter("et", endTime)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra trùng lịch phòng khi CẬP NHẬT — loại trừ schedule đang sửa.
     */
    @Override
    public boolean isRoomConflictExcluding(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
            Long excludeScheduleId) {
        EntityManager em = Jpa.em();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Schedule s " +
                            "WHERE s.room.roomId = :rid AND s.studyDate = :d " +
                            "AND s.startTime < :et AND s.endTime > :st " +
                            "AND s.scheduleId <> :exId",
                    Long.class)
                    .setParameter("rid", roomId).setParameter("d", date)
                    .setParameter("st", startTime).setParameter("et", endTime)
                    .setParameter("exId", excludeScheduleId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Schedule> findByDateRange(LocalDate from, LocalDate to) {
        EntityManager em = Jpa.em();
        try {
            // BETWEEN bao gồm cả 2 đầu: from <= studyDate <= to
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.studyDate BETWEEN :f AND :t ORDER BY s.studyDate, s.startTime",
                    Schedule.class)
                    .setParameter("f", from).setParameter("t", to).getResultList();
        } finally {
            em.close();
        }
    }
}
