package vn.edu.ute.languagecenter.management.dao;

import vn.edu.ute.languagecenter.management.model.Certificate;

import jakarta.persistence.EntityManager;
import java.util.List;

public class CertificateDAO extends GenericDAO<Certificate> {

    public CertificateDAO() {
        super(Certificate.class);
    }

    /** Lấy chứng chỉ theo học viên */
    public List<Certificate> findByStudentId(Long studentId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Certificate c WHERE c.student.studentId = :sid ORDER BY c.issueDate DESC",
                    Certificate.class)
                    .setParameter("sid", studentId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy chứng chỉ theo lớp */
    public List<Certificate> findByClassId(Long classId) {
        EntityManager em = em();
        try {
            return em.createQuery(
                    "SELECT c FROM Certificate c WHERE c.class_.classId = :cid",
                    Certificate.class)
                    .setParameter("cid", classId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
