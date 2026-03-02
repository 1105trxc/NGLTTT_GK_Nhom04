package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.Course;

import jakarta.persistence.EntityManager;
import java.util.List;

public class CourseDAO extends GenericDAO<Course> {

    public CourseDAO() {
        super(Course.class);
    }

    /** Tìm khóa học theo tên (LIKE) */
    public List<Course> findByName(String keyword) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Course c WHERE LOWER(c.courseName) LIKE LOWER(:kw)", Course.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách khóa học đang Active */
    public List<Course> findAllActive() {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Course c WHERE c.status = :st", Course.class)
                    .setParameter("st", Course.ActiveStatus.Active)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
