package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.Schedule;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ScheduleDAO extends GenericDAO<Schedule> {

    public ScheduleDAO() {
        super(Schedule.class);
    }

    /** Lấy lịch theo lớp */
    public List<Schedule> findByClassId(Long classId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.class_.classId = :cid ORDER BY s.studyDate, s.startTime",
                    Schedule.class)
                    .setParameter("cid", classId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy lịch theo phòng và ngày */
    public List<Schedule> findByRoomAndDate(Long roomId, LocalDate date) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.room.roomId = :rid AND s.studyDate = :d ORDER BY s.startTime",
                    Schedule.class)
                    .setParameter("rid", roomId)
                    .setParameter("d", date)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * (QUAN TRỌNG) Kiểm tra trùng lịch phòng học.
     * Trả về true nếu phòng đã bị chiếm trong khung giờ đó.
     * Logic: Hai khoảng thời gian giao nhau khi start1 < end2 AND start2 < end1.
     */
    public boolean isRoomConflict(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        EntityManager em = em();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Schedule s " +
                            "WHERE s.room.roomId = :rid " +
                            "AND s.studyDate = :d " +
                            "AND s.startTime < :et " +
                            "AND s.endTime > :st",
                    Long.class)
                    .setParameter("rid", roomId)
                    .setParameter("d", date)
                    .setParameter("st", startTime)
                    .setParameter("et", endTime)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra trùng lịch phòng học (loại trừ schedule hiện tại khi UPDATE).
     */
    public boolean isRoomConflictExcluding(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
            Long excludeScheduleId) {
        EntityManager em = em();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(s) FROM Schedule s " +
                            "WHERE s.room.roomId = :rid " +
                            "AND s.studyDate = :d " +
                            "AND s.startTime < :et " +
                            "AND s.endTime > :st " +
                            "AND s.scheduleId <> :exId",
                    Long.class)
                    .setParameter("rid", roomId)
                    .setParameter("d", date)
                    .setParameter("st", startTime)
                    .setParameter("et", endTime)
                    .setParameter("exId", excludeScheduleId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /** Lấy lịch theo khoảng ngày (tuần / tháng) */
    public List<Schedule> findByDateRange(LocalDate from, LocalDate to) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT s FROM Schedule s WHERE s.studyDate BETWEEN :f AND :t ORDER BY s.studyDate, s.startTime",
                    Schedule.class)
                    .setParameter("f", from)
                    .setParameter("t", to)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
