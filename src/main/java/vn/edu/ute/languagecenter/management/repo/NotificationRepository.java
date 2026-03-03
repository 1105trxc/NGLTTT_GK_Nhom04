package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Notification;

import java.util.List;
import java.util.Optional;

/** Giao diện Repository cho Notification. */
public interface NotificationRepository {
    void save(Notification notification);

    Notification update(Notification notification);

    void delete(Object id);

    Optional<Notification> findById(Object id);

    List<Notification> findAll();

    List<Notification> findRecent(int limit);
}
