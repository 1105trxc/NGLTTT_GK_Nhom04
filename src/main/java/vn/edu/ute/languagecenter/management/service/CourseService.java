package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Course;
import vn.edu.ute.languagecenter.management.repo.CourseRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaCourseRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Service xử lý nghiệp vụ Khóa học.
 * Nhận dữ liệu từ UI → validate → gọi Repository.
 */
public class CourseService {

    // Khai báo kiểu Interface, khởi tạo bằng class JPA cụ thể
    private final CourseRepository courseRepo = new JpaCourseRepository();

    /** Thêm khóa học mới (validate trước khi lưu) */
    public Course save(Course course) {
        validate(course);
        return courseRepo.save(course);
    }

    /** Cập nhật khóa học */
    public Course update(Course course) {
        validate(course);
        return courseRepo.update(course);
    }

    public Optional<Course> findById(Long id) {
        return courseRepo.findById(id);
    }

    public List<Course> findAll() {
        return courseRepo.findAll();
    }

    public List<Course> findAllActive() {
        return courseRepo.findAllActive();
    }

    public List<Course> findByName(String keyword) {
        return courseRepo.findByName(keyword);
    }

    public void deleteById(Long id) {
        courseRepo.deleteById(id);
    }

    /**
     * [LAMBDA 1] Lọc danh sách khóa học bằng Predicate lambda.
     * VD: filterCourses(c -> c.getFee().doubleValue() > 500)
     * Predicate<Course> là functional interface nhận Course, trả boolean.
     */
    public List<Course> filterCourses(Predicate<Course> condition) {
        return courseRepo.findAll().stream()
                .filter(condition) // Áp dụng lambda lọc
                .toList(); // Thu kết quả thành List
    }

    /**
     * [LAMBDA 2] Thực hiện hành động trên mỗi khóa học Active.
     * Consumer<Course> là functional interface nhận Course, không trả về gì.
     * VD: forEachActiveCourse(c -> System.out.println(c.getCourseName()))
     */
    public void forEachActiveCourse(Consumer<Course> action) {
        courseRepo.findAllActive().forEach(action);
    }

    // ---- Kiểm tra dữ liệu đầu vào ----
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
