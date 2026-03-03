package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.repo.ClassRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaClassRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ Lớp học.
 */
public class ClassService {

    private final ClassRepository classRepo = new JpaClassRepository();

    public Class_ save(Class_ cls) {
        validate(cls);
        return classRepo.save(cls);
    }

    public Class_ update(Class_ cls) {
        validate(cls);
        return classRepo.update(cls);
    }

    public Optional<Class_> findById(Long id) {
        return classRepo.findById(id);
    }

    public List<Class_> findAll() {
        return classRepo.findAll();
    }

    public List<Class_> findByCourseId(Long courseId) {
        return classRepo.findByCourseId(courseId);
    }

    public List<Class_> findByTeacherId(Long teacherId) {
        return classRepo.findByTeacherId(teacherId);
    }

    public List<Class_> findByStatus(Class_.ClassStatus status) {
        return classRepo.findByStatus(status);
    }

    public List<Class_> findByName(String keyword) {
        return classRepo.findByName(keyword);
    }

    public long countEnrolledStudents(Long classId) {
        return classRepo.countEnrolledStudents(classId);
    }

    public void deleteById(Long id) {
        classRepo.deleteById(id);
    }

    /**
     * [LAMBDA 4] Nhóm các lớp theo trạng thái bằng Collectors.groupingBy.
     * Lambda: c -> c.getStatus() trích xuất key để nhóm.
     * Kết quả Map<ClassStatus, List<Class_>>
     */
    public Map<Class_.ClassStatus, List<Class_>> groupByStatus() {
        return classRepo.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getStatus()));
    }

    // ---- Kiểm tra dữ liệu ----
    private void validate(Class_ cls) {
        if (cls.getClassName() == null || cls.getClassName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên lớp không được để trống.");
        }
        if (cls.getCourse() == null) {
            throw new IllegalArgumentException("Phải chọn khóa học cho lớp.");
        }
        if (cls.getStartDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống.");
        }
        if (cls.getEndDate() != null && cls.getEndDate().isBefore(cls.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        if (cls.getMaxStudent() == null || cls.getMaxStudent() <= 0) {
            throw new IllegalArgumentException("Sĩ số tối đa phải lớn hơn 0.");
        }
    }
}
