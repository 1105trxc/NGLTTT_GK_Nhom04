package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaTeacherRepository;
import vn.edu.ute.languagecenter.management.model.Teacher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TeacherService - Xử lý nghiệp vụ quản lý hồ sơ giáo viên.
 */
public class TeacherService {

    private final JpaTeacherRepository teacherDAO = new JpaTeacherRepository();

    /**
     * Lấy tất cả giáo viên.
     */
    public List<Teacher> getAllTeachers() {
        return teacherDAO.findAll();
    }

    /**
     * Lấy tất cả giáo viên đang Active.
     */
    public List<Teacher> getActiveTeachers() {
        return teacherDAO.findAllActive();
    }

    /**
     * Tìm giáo viên theo ID.
     */
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherDAO.findById(id);
    }

    /**
     * Thêm mới giáo viên vào database.
     * Tự động set created_at và updated_at.
     *
     * @param teacher đối tượng Teacher cần lưu
     */
    public void addTeacher(Teacher teacher) {
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());
        if (teacher.getStatus() == null) {
            teacher.setStatus(Teacher.ActiveStatus.Active);
        }
        teacherDAO.save(teacher);
    }

    /**
     * Cập nhật thông tin giáo viên.
     *
     * @param teacher đối tượng Teacher đã chỉnh sửa
     */
    public Teacher updateTeacher(Teacher teacher) {
        teacher.setUpdatedAt(LocalDateTime.now());
        return teacherDAO.update(teacher);
    }

    /**
     * Xóa giáo viên theo ID.
     *
     * @param id ID của giáo viên cần xóa
     */
    public void deleteTeacher(Long id) {
        teacherDAO.delete(id);
    }
}
