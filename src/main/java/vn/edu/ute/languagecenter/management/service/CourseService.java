package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.dao.CourseDAO;
import vn.edu.ute.languagecenter.management.model.Course;

import java.util.List;
import java.util.Optional;

public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();

    public Course save(Course course) {
        validate(course);
        return courseDAO.save(course);
    }

    public Course update(Course course) {
        validate(course);
        return courseDAO.update(course);
    }

    public Optional<Course> findById(Long id) {
        return courseDAO.findById(id);
    }

    public List<Course> findAll() {
        return courseDAO.findAll();
    }

    public List<Course> findAllActive() {
        return courseDAO.findAllActive();
    }

    public List<Course> findByName(String keyword) {
        return courseDAO.findByName(keyword);
    }

    public void deleteById(Long id) {
        courseDAO.deleteById(id);
    }

    // ---- Validation ----
    private void validate(Course course) {
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khóa học không được để trống.");
        }
        if (course.getFee() == null || course.getFee().signum() < 0) {
            throw new IllegalArgumentException("Học phí không hợp lệ (phải >= 0).");
        }
        if (course.getDuration() != null && course.getDuration() <= 0) {
            throw new IllegalArgumentException("Thời lượng phải lớn hơn 0.");
        }
    }
}
