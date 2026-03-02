package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.dao.UserAccountDAO;
import vn.edu.ute.languagecenter.management.dao.TeacherDAO;
import vn.edu.ute.languagecenter.management.dao.StaffDAO;
import vn.edu.ute.languagecenter.management.dao.StudentDAO;
import vn.edu.ute.languagecenter.management.model.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UserService - Xử lý nghiệp vụ liên quan đến tài khoản người dùng:
 * - Đăng nhập / đăng xuất
 * - Tạo tài khoản liên kết với Teacher / Staff / Student
 * - Khoá / mở khoá tài khoản
 */
public class UserService {

    private final UserAccountDAO userDAO = new UserAccountDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Xác thực đăng nhập.
     * 
     * @param username tên đăng nhập
     * @param password mật khẩu (plain-text, nên hash trước trong production)
     * @return Optional<UserAccount> nếu đúng thông tin, empty nếu sai
     */
    public Optional<UserAccount> login(String username, String password) {
        return userDAO.login(username, password);
    }

    /**
     * Tạo mới UserAccount liên kết với một Teacher đã tồn tại.
     * Rule: role = Teacher, teacher_id != null, student_id = null, staff_id = null
     *
     * @param username  tên đăng nhập mới
     * @param password  mật khẩu (plain-text)
     * @param teacherId ID của giáo viên cần liên kết
     */
    public void createTeacherAccount(String username, String password, Long teacherId) {
        // Kiểm tra teacher tồn tại
        Teacher teacher = teacherDAO.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giáo viên ID=" + teacherId));

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(password); // nên hash MD5/BCrypt trong production
        account.setRole(UserAccount.UserRole.Teacher);
        account.setTeacher(teacher); // liên kết @OneToOne với Teacher
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        userDAO.save(account);
    }

    /**
     * Tạo mới UserAccount liên kết với một Staff đã tồn tại.
     * Rule: role = Staff, staff_id != null, teacher_id = null, student_id = null
     *
     * @param username tên đăng nhập mới
     * @param password mật khẩu
     * @param staffId  ID của nhân viên cần liên kết
     */
    public void createStaffAccount(String username, String password, Long staffId) {
        Staff staff = staffDAO.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên ID=" + staffId));

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(password);
        account.setRole(UserAccount.UserRole.Staff);
        account.setStaff(staff); // liên kết @OneToOne với Staff
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        userDAO.save(account);
    }

    /**
     * Tạo mới UserAccount liên kết với một Student đã tồn tại.
     * Rule: role = Student, student_id != null, teacher_id = null, staff_id = null
     *
     * @param username  tên đăng nhập mới
     * @param password  mật khẩu
     * @param studentId ID của học viên cần liên kết
     */
    public void createStudentAccount(String username, String password, Long studentId) {
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học viên ID=" + studentId));

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(password);
        account.setRole(UserAccount.UserRole.Student);
        account.setStudent(student); // liên kết @OneToOne với Student
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        userDAO.save(account);
    }

    /**
     * Tạo tài khoản Admin (không cần liên kết với Teacher/Staff/Student).
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     */
    public void createAdminAccount(String username, String password) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(password);
        account.setRole(UserAccount.UserRole.Admin);
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        userDAO.save(account);
    }

    /**
     * Khoá tài khoản theo ID (đặt isActive = false).
     * 
     * @param userId ID tài khoản cần khoá
     */
    public void lockAccount(Long userId) {
        UserAccount account = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản ID=" + userId));
        account.setIsActive(false);
        account.setUpdatedAt(LocalDateTime.now());
        userDAO.update(account);
    }

    /**
     * Mở khoá tài khoản theo ID (đặt isActive = true).
     * 
     * @param userId ID tài khoản cần mở khoá
     */
    public void unlockAccount(Long userId) {
        UserAccount account = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản ID=" + userId));
        account.setIsActive(true);
        account.setUpdatedAt(LocalDateTime.now());
        userDAO.update(account);
    }
}
