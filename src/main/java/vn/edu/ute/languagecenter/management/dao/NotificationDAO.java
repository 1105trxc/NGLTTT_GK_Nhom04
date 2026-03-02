package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Notification;

import java.util.List;

/**
 * NotificationDAO - DAO chuyên biệt cho entity Notification (Thông báo).
 * Kế thừa GenericDAO, bổ sung truy vấn liên quan đến phân loại thông báo theo
 * role.
 */
public class NotificationDAO extends GenericDAO<Notification> {

    public NotificationDAO() {
        super(Notification.class);
    }

    /**
     * Lấy N thông báo gần đây nhất dành cho một role cụ thể hoặc tất cả.
     * Dùng trên trang Dashboard để hiển thị bảng thông báo.
     *
     * @param role  role người dùng đang đăng nhập (All / Student / Teacher / Staff
     *              / Admin)
     * @param limit số lượng thông báo tối đa cần lấy (ví dụ: 5 hoặc 10)
     * @return danh sách Notification theo role và thời gian mới nhất
     */
    public List<Notification> findRecentByRole(Notification.TargetRole role, int limit) {
        EntityManager em = getEntityManager();
        try {
            // Lọc thông báo dành cho "All" hoặc đúng role người dùng đang đăng nhập
            String hql = "SELECT n FROM Notification n " +
                    "WHERE n.targetRole = 'All' OR n.targetRole = :role " +
                    "ORDER BY n.createdAt DESC";
            TypedQuery<Notification> q = em.createQuery(hql, Notification.class);
            q.setParameter("role", role);
            q.setMaxResults(limit); // giới hạn số bản ghi trả về
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả thông báo mới nhất, không lọc role.
     * 
     * @return toàn bộ Notification sắp xếp theo thời gian tạo giảm dần
     */
    public List<Notification> findAllRecent() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT n FROM Notification n ORDER BY n.createdAt DESC";
            TypedQuery<Notification> q = em.createQuery(hql, Notification.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
