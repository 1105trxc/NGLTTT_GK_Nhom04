package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaUserAccountRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaTeacherRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaStaffRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaStudentRepository;
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

    private final JpaUserAccountRepository userDAO = new JpaUserAccountRepository();
    private final JpaTeacherRepository teacherDAO = new JpaTeacherRepository();
    private final JpaStaffRepository staffDAO = new JpaStaffRepository();
    private final JpaStudentRepository studentDAO = new JpaStudentRepository();

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
        
        // Kiểm tra giáo viên này đã có tài khoản chưa
        boolean alreadyHasAccount = findAllUsersWithLinks().stream()
                .anyMatch(u -> u.getTeacher() != null && u.getTeacher().getTeacherId().equals(teacherId));
        if (alreadyHasAccount) {
            throw new IllegalArgumentException("Người dùng này đã có tài khoản");
        }
        
        // Kiểm tra trùng username
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
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

        // Kiểm tra nhân viên này đã có tài khoản chưa
        boolean alreadyHasAccount = findAllUsersWithLinks().stream()
                .anyMatch(u -> u.getStaff() != null && u.getStaff().getStaffId().equals(staffId));
        if (alreadyHasAccount) {
            throw new IllegalArgumentException("Người dùng này đã có tài khoản");
        }
        
        // Kiểm tra trùng username
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
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

        // Kiểm tra học viên này đã có tài khoản chưa
        boolean alreadyHasAccount = findAllUsersWithLinks().stream()
                .anyMatch(u -> u.getStudent() != null && u.getStudent().getStudentId().equals(studentId));
        if (alreadyHasAccount) {
            throw new IllegalArgumentException("Người dùng này đã có tài khoản");
        }
        
        // Kiểm tra trùng username
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
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
        // Kiểm tra trùng username
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
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

    /**
     * Tạo tài khoản đơn giản — chỉ cần username, password, role.
     * Không bắt buộc liên kết với Teacher/Staff/Student.
     */
    public void createAccount(String username, String password, UserAccount.UserRole role) {
        // Kiểm tra trùng username
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt()));
        account.setRole(role);
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        userDAO.save(account);
    }

    /**
     * Lấy danh sách tất cả tài khoản.
     */
    public java.util.List<UserAccount> findAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Lấy danh sách tài khoản kèm theo thông tin liên kết đã được load eager
     * (tránh LazyInitializationException khi truy cập teacher/staff/student).
     */
    public java.util.List<UserAccount> findAllUsersWithLinks() {
        return userDAO.findAllWithLinks();
    }

    /**
     * Đổi mật khẩu tài khoản theo ID.
     *
     * @param userId      ID tài khoản
     * @param newPassword mật khẩu mới (plain-text)
     */
    public void changePassword(Long userId, String newPassword) {
        UserAccount account = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản ID=" + userId));
        account.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt()));
        account.setUpdatedAt(LocalDateTime.now());
        userDAO.update(account);
    }
}
