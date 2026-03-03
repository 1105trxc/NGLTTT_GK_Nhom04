package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Certificate;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho Certificate (Chứng chỉ).
 */
public interface CertificateRepository {
    Certificate save(Certificate cert); // Cấp chứng chỉ mới

    Certificate update(Certificate cert); // Cập nhật

    Optional<Certificate> findById(Long id); // Tìm theo ID

    List<Certificate> findAll(); // Lấy tất cả

    void deleteById(Long id); // Xóa

    List<Certificate> findByStudentId(Long studentId); // Lọc theo học viên

    List<Certificate> findByClassId(Long classId); // Lọc theo lớp
}
