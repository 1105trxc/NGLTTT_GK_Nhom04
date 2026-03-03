package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Staff;
import vn.edu.ute.languagecenter.management.repo.StaffRepository;

import java.util.List;

/** JPA triển khai StaffRepository. */
public class JpaStaffRepository extends GenericRepository<Staff>
        implements StaffRepository {

    public JpaStaffRepository() {
        super(Staff.class);
    }

    /** Lấy tất cả nhân viên, sắp xếp theo tên A-Z. */
    public List<Staff> findAllSorted() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Staff s ORDER BY s.fullName ASC", Staff.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy nhân viên đang Active, sắp xếp theo tên. */
    public List<Staff> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Staff s WHERE s.status = 'Active' ORDER BY s.fullName ASC", Staff.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
