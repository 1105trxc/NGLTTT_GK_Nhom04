package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.Notification;
import vn.edu.ute.languagecenter.management.repo.NotificationRepository;

import java.util.List;

/** JPA triển khai NotificationRepository. */
public class JpaNotificationRepository extends GenericRepository<Notification>
        implements NotificationRepository {

    public JpaNotificationRepository() {
        super(Notification.class);
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
