package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.PlacementTest;
import vn.edu.ute.languagecenter.management.model.Certificate;
import vn.edu.ute.languagecenter.management.repo.PlacementTestRepository;
import vn.edu.ute.languagecenter.management.repo.CertificateRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaPlacementTestRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaCertificateRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service xử lý nghiệp vụ Kiểm tra đầu vào (PlacementTest) và Chứng chỉ
 * (Certificate).
 * Gộp 2 chức năng liên quan vào cùng 1 service.
 */
public class ExamAndCertService {

    // Khai báo kiểu Interface, khởi tạo bằng JPA implementation
    private final PlacementTestRepository testRepo = new JpaPlacementTestRepository();
    private final CertificateRepository certRepo = new JpaCertificateRepository();

    // ========== PlacementTest ==========

    /** Lưu kết quả test — tự động gợi ý cấp độ nếu có điểm */
    public PlacementTest saveTest(PlacementTest test) {
        validateTest(test);
        if (test.getScore() != null && test.getSuggestedLevel() == null) {
            test.setSuggestedLevel(suggestLevel(test.getScore()));
        }
        return testRepo.save(test);
    }

    public Optional<PlacementTest> findTestById(Long id) {
        return testRepo.findById(id);
    }

    public List<PlacementTest> findTestsByStudentId(Long studentId) {
        return testRepo.findByStudentId(studentId);
    }

    public List<PlacementTest> findAllTests() {
        return testRepo.findAll();
    }

    public void deleteTestById(Long id) {
        testRepo.deleteById(id);
    }

    /**
     * [LAMBDA 6] Chuyển đổi danh sách test thành danh sách tên cấp độ.
     * Dùng Function<PlacementTest, String> lambda: nhận test → trả chuỗi level.
     * Stream.map() áp dụng function lên từng phần tử.
     */
    public List<String> mapTestsToSuggestedLevels() {
        Function<PlacementTest, String> toLevelName = test -> test.getSuggestedLevel() != null
                ? test.getSuggestedLevel()
                : "Chưa xác định";
        return testRepo.findAll().stream()
                .map(toLevelName) // Áp dụng Function lambda
                .toList();
    }

    // ========== Certificate ==========

    /** Cấp chứng chỉ mới */
    public Certificate saveCertificate(Certificate cert) {
        validateCert(cert);
        return certRepo.save(cert);
    }

    public Optional<Certificate> findCertById(Long id) {
        return certRepo.findById(id);
    }

    public List<Certificate> findCertsByStudentId(Long studentId) {
        return certRepo.findByStudentId(studentId);
    }

    public List<Certificate> findCertsByClassId(Long classId) {
        return certRepo.findByClassId(classId);
    }

    public List<Certificate> findAllCerts() {
        return certRepo.findAll();
    }

    public void deleteCertById(Long id) {
        certRepo.deleteById(id);
    }

    /**
     * [LAMBDA 7] Đếm số chứng chỉ cấp trong năm hiện tại.
     * Lambda filter: c -> c.getIssueDate().getYear() == currentYear
     * Stream.count() đếm số phần tử thỏa điều kiện.
     */
    public long countCertsIssuedThisYear() {
        int currentYear = java.time.LocalDate.now().getYear();
        return certRepo.findAll().stream()
                .filter(c -> c.getIssueDate() != null && c.getIssueDate().getYear() == currentYear)
                .count();
    }

    // ---- Business Logic: Gợi ý cấp độ theo điểm ----
    private String suggestLevel(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 7.0)
            return "Advanced"; // >= 7 → Nâng cao
        if (s >= 4.0)
            return "Intermediate"; // >= 4 → Trung cấp
        return "Beginner"; // < 4 → Sơ cấp
    }

    // ---- Kiểm tra dữ liệu ----
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
