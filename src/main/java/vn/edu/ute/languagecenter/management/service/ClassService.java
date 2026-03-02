package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.dao.ClassDAO;
import vn.edu.ute.languagecenter.management.model.Class_;

import java.util.List;
import java.util.Optional;

public class ClassService {

    private final ClassDAO classDAO = new ClassDAO();

    public Class_ save(Class_ cls) {
        validate(cls);
        return classDAO.save(cls);
    }

    public Class_ update(Class_ cls) {
        validate(cls);
        return classDAO.update(cls);
    }

    public Optional<Class_> findById(Long id) {
        return classDAO.findById(id);
    }

    public List<Class_> findAll() {
        return classDAO.findAll();
    }

    public List<Class_> findByCourseId(Long courseId) {
        return classDAO.findByCourseId(courseId);
    }

    public List<Class_> findByTeacherId(Long teacherId) {
        return classDAO.findByTeacherId(teacherId);
    }

    public List<Class_> findByStatus(Class_.ClassStatus status) {
        return classDAO.findByStatus(status);
    }

    public List<Class_> findByName(String keyword) {
        return classDAO.findByName(keyword);
    }

    public long countEnrolledStudents(Long classId) {
        return classDAO.countEnrolledStudents(classId);
    }

    public void deleteById(Long id) {
        classDAO.deleteById(id);
    }

    // ---- Validation ----
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
