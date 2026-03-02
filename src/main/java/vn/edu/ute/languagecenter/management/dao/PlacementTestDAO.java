package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.PlacementTest;

import jakarta.persistence.EntityManager;
import java.util.List;

public class PlacementTestDAO extends GenericDAO<PlacementTest> {

    public PlacementTestDAO() {
        super(PlacementTest.class);
    }

    /** Lấy kết quả test theo học viên */
    public List<PlacementTest> findByStudentId(Long studentId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT p FROM PlacementTest p WHERE p.student.studentId = :sid ORDER BY p.testDate DESC",
                    PlacementTest.class)
                    .setParameter("sid", studentId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
