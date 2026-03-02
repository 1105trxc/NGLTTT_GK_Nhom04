package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * StudentDAO - DAO chuyên biệt cho entity Student (Học viên).
 * Kế thừa GenericDAO, bổ sung các truy vấn HQL đặc thù.
 */
public class StudentDAO extends GenericDAO<Student> {

    public StudentDAO() {
        super(Student.class);
    }

    /**
     * Lấy tất cả học viên, sắp xếp theo tên A-Z.
     * 
     * @return danh sách học viên
     */
    public List<Student> findAllSorted() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Student s ORDER BY s.fullName ASC";
            TypedQuery<Student> q = em.createQuery(hql, Student.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm học viên theo email (kiểm tra duplicate khi tạo mới).
     * 
     * @param email địa chỉ email
     * @return Optional<Student>
     */
    public Optional<Student> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Student s WHERE s.email = :email";
            TypedQuery<Student> q = em.createQuery(hql, Student.class);
            q.setParameter("email", email);
            List<Student> result = q.getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm tất cả học viên đăng ký trong một năm cụ thể.
     * 
     * @param year năm cần lọc (ví dụ: 2025)
     * @return danh sách học viên đăng ký trong năm đó
     */
    public List<Student> findByRegistrationYear(int year) {
        EntityManager em = getEntityManager();
        try {
            // Lọc học viên registrationDate trong khoảng đầu và cuối năm
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            String hql = "SELECT s FROM Student s WHERE s.registrationDate BETWEEN :start AND :end " +
                    "ORDER BY s.registrationDate DESC";
            TypedQuery<Student> q = em.createQuery(hql, Student.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả học viên đang Active, sắp xếp theo ngày đăng ký mới nhất.
     * 
     * @return danh sách Student Active
     */
    public List<Student> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Student s WHERE s.status = 'Active' ORDER BY s.registrationDate DESC";
            TypedQuery<Student> q = em.createQuery(hql, Student.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
