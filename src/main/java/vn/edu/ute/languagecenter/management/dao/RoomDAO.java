package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.Room;

import jakarta.persistence.EntityManager;
import java.util.List;

public class RoomDAO extends GenericDAO<Room> {

    public RoomDAO() {
        super(Room.class);
    }

    /** Tìm phòng theo tên */
    public List<Room> findByName(String keyword) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT r FROM Room r WHERE LOWER(r.roomName) LIKE LOWER(:kw)", Room.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách phòng đang Active */
    public List<Room> findAllActive() {
        EntityManager em = em();
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
