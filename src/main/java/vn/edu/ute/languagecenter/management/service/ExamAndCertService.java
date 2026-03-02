package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.PlacementTest;
import vn.edu.ute.languagecenter.management.dao.CertificateDAO;
import vn.edu.ute.languagecenter.management.dao.PlacementTestDAO;
import vn.edu.ute.languagecenter.management.model.Certificate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExamAndCertService {

    private final PlacementTestDAO placementTestDAO = new PlacementTestDAO();
    private final CertificateDAO certificateDAO = new CertificateDAO();

    // ========== PlacementTest ==========

    public PlacementTest saveTest(PlacementTest test) {
        validateTest(test);
        // Tự động gợi ý cấp độ theo điểm
        if (test.getScore() != null && test.getSuggestedLevel() == null) {
            test.setSuggestedLevel(suggestLevel(test.getScore()));
        }
        return placementTestDAO.save(test);
    }

    public PlacementTest updateTest(PlacementTest test) {
        validateTest(test);
        if (test.getScore() != null && test.getSuggestedLevel() == null) {
            test.setSuggestedLevel(suggestLevel(test.getScore()));
        }
        return placementTestDAO.update(test);
    }

    public Optional<PlacementTest> findTestById(Long id) {
        return placementTestDAO.findById(id);
    }

    public List<PlacementTest> findTestsByStudentId(Long studentId) {
        return placementTestDAO.findByStudentId(studentId);
    }

    public List<PlacementTest> findAllTests() {
        return placementTestDAO.findAll();
    }

    public void deleteTestById(Long id) {
        placementTestDAO.deleteById(id);
    }

    // ========== Certificate ==========

    public Certificate saveCertificate(Certificate cert) {
        validateCert(cert);
        return certificateDAO.save(cert);
    }

    public Certificate updateCertificate(Certificate cert) {
        validateCert(cert);
        return certificateDAO.update(cert);
    }

    public Optional<Certificate> findCertById(Long id) {
        return certificateDAO.findById(id);
    }

    public List<Certificate> findCertsByStudentId(Long studentId) {
        return certificateDAO.findByStudentId(studentId);
    }

    public List<Certificate> findCertsByClassId(Long classId) {
        return certificateDAO.findByClassId(classId);
    }

    public List<Certificate> findAllCerts() {
        return certificateDAO.findAll();
    }

    public void deleteCertById(Long id) {
        certificateDAO.deleteById(id);
    }

    // ---- Business Logic ----

    /** Gợi ý cấp độ theo điểm thi đầu vào */
    private String suggestLevel(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 7.0)
            return "Advanced";
        if (s >= 4.0)
            return "Intermediate";
        return "Beginner";
    }

    // ---- Validation ----

    private void validateTest(PlacementTest test) {
        if (test.getStudent() == null) {
            throw new IllegalArgumentException("Phải chọn học viên.");
        }
        if (test.getTestDate() == null) {
            throw new IllegalArgumentException("Ngày thi không được để trống.");
        }
    }

    private void validateCert(Certificate cert) {
        if (cert.getStudent() == null) {
            throw new IllegalArgumentException("Phải chọn học viên.");
        }
        if (cert.getCertName() == null || cert.getCertName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chứng chỉ không được để trống.");
        }
        if (cert.getIssueDate() == null) {
            throw new IllegalArgumentException("Ngày cấp không được để trống.");
        }
    }
}
