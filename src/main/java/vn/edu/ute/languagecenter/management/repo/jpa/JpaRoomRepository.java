package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.db.Jpa;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.model.Room;
import vn.edu.ute.languagecenter.management.repo.RoomRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai RoomRepository bằng JPA/Hibernate.
 */
public class JpaRoomRepository implements RoomRepository {

    private final TransactionManager txm = new TransactionManager();

    @Override
    public Room save(Room room) {
        try {
            return txm.runInTransaction(em -> {
                em.persist(room);
                return room;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Room update(Room room) {
        try {
            return txm.runInTransaction(em -> em.merge(room));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Room> findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return Optional.ofNullable(em.find(Room.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Room> findAll() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery("SELECT r FROM Room r", Room.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            txm.runInTransaction(em -> {
                Room r = em.find(Room.class, id);
                if (r != null)
                    em.remove(r);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Room> findByName(String keyword) {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT r FROM Room r WHERE LOWER(r.roomName) LIKE LOWER(:kw)", Room.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Room> findAllActive() {
        EntityManager em = Jpa.em();
        try {
            return em.createQuery(
                    "SELECT r FROM Room r WHERE r.status = :st", Room.class)
                    .setParameter("st", Room.ActiveStatus.Active)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
