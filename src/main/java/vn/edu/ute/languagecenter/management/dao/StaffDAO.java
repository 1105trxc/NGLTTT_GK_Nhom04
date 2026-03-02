package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Staff;

import java.util.List;
import java.util.Optional;

/**
 * StaffDAO - DAO chuyên biệt cho entity Staff (Nhân viên).
 * Kế thừa GenericDAO, bổ sung các truy vấn HQL đặc thù.
 */
public class StaffDAO extends GenericDAO<Staff> {

    public StaffDAO() {
        super(Staff.class);
    }

    /**
     * Lấy toàn bộ nhân viên, sắp xếp theo tên A-Z.
     * 
     * @return danh sách nhân viên
     */
    public List<Staff> findAllSorted() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Staff s ORDER BY s.fullName ASC";
            TypedQuery<Staff> q = em.createQuery(hql, Staff.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm nhân viên theo chức vụ (role).
     * 
     * @param role chức vụ cần lọc (Admin, Consultant, Accountant, Manager, Other)
     * @return danh sách nhân viên có role tương ứng
     */
    public List<Staff> findByRole(Staff.StaffRole role) {
        EntityManager em = getEntityManager();
        try {
            // HQL JPQL lọc nhân viên theo role
            String hql = "SELECT s FROM Staff s WHERE s.role = :role ORDER BY s.fullName ASC";
            TypedQuery<Staff> q = em.createQuery(hql, Staff.class);
            q.setParameter("role", role);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm nhân viên theo email (kiểm tra trùng khi đăng ký).
     * 
     * @param email địa chỉ email cần tìm
     * @return Optional<Staff>
     */
    public Optional<Staff> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Staff s WHERE s.email = :email";
            TypedQuery<Staff> q = em.createQuery(hql, Staff.class);
            q.setParameter("email", email);
            List<Staff> result = q.getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả nhân viên đang làm việc (Active).
     * 
     * @return danh sách Staff Active
     */
    public List<Staff> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT s FROM Staff s WHERE s.status = 'Active' ORDER BY s.fullName ASC";
            TypedQuery<Staff> q = em.createQuery(hql, Staff.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
