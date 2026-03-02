package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.Class_;

import jakarta.persistence.EntityManager;
import java.util.List;

public class ClassDAO extends GenericDAO<Class_> {

    public ClassDAO() {
        super(Class_.class);
    }

    /** Lấy danh sách lớp theo khóa học */
    public List<Class_> findByCourseId(Long courseId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.course.courseId = :cid", Class_.class)
                    .setParameter("cid", courseId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách lớp theo giáo viên */
    public List<Class_> findByTeacherId(Long teacherId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.teacher.teacherId = :tid", Class_.class)
                    .setParameter("tid", teacherId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách lớp theo trạng thái */
    public List<Class_> findByStatus(Class_.ClassStatus status) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE c.status = :st", Class_.class)
                    .setParameter("st", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Tìm kiếm lớp theo tên */
    public List<Class_> findByName(String keyword) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Class_ c WHERE LOWER(c.className) LIKE LOWER(:kw)", Class_.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Đếm số học viên đã enrolled trong lớp (phục vụ kiểm tra sĩ số) */
    public long countEnrolledStudents(Long classId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT COUNT(e) FROM Enrollment e WHERE e.class_.classId = :cid AND e.status = 'Enrolled'",
                    Long.class)
                    .setParameter("cid", classId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
