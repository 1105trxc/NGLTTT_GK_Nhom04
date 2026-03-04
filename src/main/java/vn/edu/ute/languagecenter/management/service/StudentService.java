package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaStudentRepository;
import vn.edu.ute.languagecenter.management.model.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StudentService - Xử lý nghiệp vụ quản lý hồ sơ học viên.
 */
public class StudentService {

    private final JpaStudentRepository studentDAO = new JpaStudentRepository();

    /**
     * Lấy tất cả học viên.
     */
    public List<Student> getAllStudents() {
        return studentDAO.findAllSorted();
    }

    /**
     * Lấy học viên đang Active.
     */
    public List<Student> getActiveStudents() {
        return studentDAO.findAllActive();
    }

    /**
     * Tìm học viên theo ID.
     */
    public Optional<Student> getStudentById(Long id) {
        return studentDAO.findById(id);
    }

    /**
     * Thêm mới học viên vào database.
     *
     * @param student đối tượng Student cần lưu
     */
    public void addStudent(Student student) {
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        if (student.getRegistrationDate() == null) {
            student.setRegistrationDate(LocalDate.now()); // mặc định ngày đăng ký là hôm nay
        }
        if (student.getStatus() == null) {
            student.setStatus(Student.ActiveStatus.Active);
        }
        studentDAO.save(student);
    }

    /**
     * Cập nhật thông tin học viên.
     *
     * @param student đối tượng Student đã chỉnh sửa
     */
    public Student updateStudent(Student student) {
        student.setUpdatedAt(LocalDateTime.now());
        return studentDAO.update(student);
    }

    /**
     * Xóa học viên theo ID.
     *
     * @param id ID của học viên cần xóa
     */
    public void deleteStudent(Long id) {
        studentDAO.delete(id);
    }
}
