package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Notification;
import vn.edu.ute.languagecenter.management.repo.NotificationRepository;

import jakarta.persistence.EntityTransaction;
import java.util.List;

/** JPA triển khai NotificationRepository. */
public class JpaNotificationRepository extends GenericRepository<Notification>
        implements NotificationRepository {

    public JpaNotificationRepository() {
        super(Notification.class);
    }

    @Override
    public void save(Notification notification) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (notification.getCreatedByUser() != null) {
                vn.edu.ute.languagecenter.management.model.UserAccount attachedUser = em.find(
                        vn.edu.ute.languagecenter.management.model.UserAccount.class,
                        notification.getCreatedByUser().getUserId());
                notification.setCreatedByUser(attachedUser);
            }
            em.persist(notification);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi khi lưu notification: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Notification> findRecent(int limit) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT n FROM Notification n ORDER BY n.createdAt DESC";
            TypedQuery<Notification> q = em.createQuery(hql, Notification.class);
            q.setMaxResults(limit);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
