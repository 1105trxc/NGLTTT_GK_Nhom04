package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Teacher;
import vn.edu.ute.languagecenter.management.repo.TeacherRepository;

import java.util.List;
import java.util.Optional;

/**
 * JpaTeacherRepository — triển khai TeacherRepository bằng JPA/Hibernate.
 * Kế thừa GenericDAO để tái sử dụng CRUD, implement interface để Service
 * chỉ phụ thuộc vào abstraction (Repository pattern).
 */
public class JpaTeacherRepository extends GenericRepository<Teacher>
        implements TeacherRepository {

    public JpaTeacherRepository() {
        super(Teacher.class);
    }

    /**
     * Tìm tất cả giáo viên theo trạng thái làm việc.
     *
     * @param status trạng thái (Active / Inactive)
     * @return danh sách giáo viên khớp trạng thái
     */
    @Override
    public List<Teacher> findByStatus(Teacher.ActiveStatus status) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT t FROM Teacher t WHERE t.status = :status";
            TypedQuery<Teacher> q = em.createQuery(hql, Teacher.class);
            q.setParameter("status", status);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm giáo viên theo email (kiểm tra duplicate khi tạo mới).
     *
     * @param email địa chỉ email cần tìm
     * @return Optional<Teacher>
     */
    @Override
    public Optional<Teacher> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT t FROM Teacher t WHERE t.email = :email";
            TypedQuery<Teacher> q = em.createQuery(hql, Teacher.class);
            q.setParameter("email", email);
            List<Teacher> result = q.getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm giáo viên theo chuyên môn, không phân biệt hoa thường.
     *
     * @param specialty ví dụ: "IELTS", "TOEIC"
     * @return danh sách giáo viên có chuyên môn tương ứng
     */
    @Override
    public List<Teacher> findBySpecialty(String specialty) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT t FROM Teacher t WHERE LOWER(t.specialty) LIKE LOWER(:sp)";
            TypedQuery<Teacher> q = em.createQuery(hql, Teacher.class);
            q.setParameter("sp", "%" + specialty + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả giáo viên Active, sắp xếp theo tên A-Z.
     *
     * @return danh sách giáo viên Active
     */
    @Override
    public List<Teacher> findAllActive() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT t FROM Teacher t WHERE t.status = 'Active' ORDER BY t.fullName ASC";
            TypedQuery<Teacher> q = em.createQuery(hql, Teacher.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
