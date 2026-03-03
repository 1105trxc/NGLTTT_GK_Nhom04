package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.repo.jpa.JpaStudentRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaTeacherRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaStaffRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaUserAccountRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaNotificationRepository;
import vn.edu.ute.languagecenter.management.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

/**
 * HRQueryService - Service chứa 7 câu truy vấn nghiệp vụ sử dụng Java Stream &
 * Lambda.
 *
 * Mỗi phương thức stream dữ liệu từ DB (qua DAO) rồi dùng Java 8 Stream API để
 * xử lý.
 * Có chú thích chi tiết từng dòng lệnh.
 */
public class HRQueryService {

    // Khai báo các DAO cần dùng
    private final JpaTeacherRepository teacherDAO = new JpaTeacherRepository();
    private final JpaStaffRepository staffDAO = new JpaStaffRepository();
    private final JpaStudentRepository studentDAO = new JpaStudentRepository();
    private final JpaUserAccountRepository userDAO = new JpaUserAccountRepository();
    private final JpaNotificationRepository notiDAO = new JpaNotificationRepository();

    // ============================================================
    // QUERY 1: Lọc giáo viên đang Active và dạy môn IELTS
    // ============================================================
    /**
     * [Query 1] Trả về danh sách giáo viên có status=Active VÀ chuyên môn chứa từ
     * "IELTS".
     *
     * @return List<Teacher> danh sách giáo viên IELTS đang làm việc
     */
    public List<Teacher> getActiveIELTSTeachers() {
        List<Teacher> allTeachers = teacherDAO.findAll(); // lấy tất cả giáo viên từ DB

        return allTeachers.stream()
                // Bước 1: Lọc giáo viên có status là Active
                .filter(t -> t.getStatus() == Teacher.ActiveStatus.Active)
                // Bước 2: Lọc tiếp những ai có chuyên môn chứa "IELTS" (không phân biệt hoa
                // thường)
                .filter(t -> t.getSpecialty() != null && t.getSpecialty().toUpperCase().contains("IELTS"))
                // Bước 3: Sắp xếp theo tên giáo viên A-Z
                .sorted(Comparator.comparing(Teacher::getFullName))
                // Bước 4: Gom kết quả thành List
                .collect(Collectors.toList());
    }

    // ============================================================
    // QUERY 2: Gom nhóm nhân viên theo chức vụ (Role)
    // ============================================================
    /**
     * [Query 2] Gom nhóm (Group By) danh sách Staff theo trường role.
     * Kết quả: Map với key là StaffRole, value là List<Staff> cùng chức vụ.
     *
     * @return Map<Staff.StaffRole, List<Staff>>
     */
    public Map<Staff.StaffRole, List<Staff>> groupStaffByRole() {
        List<Staff> allStaff = staffDAO.findAll(); // lấy tất cả nhân viên từ DB

        return allStaff.stream()
                // Collectors.groupingBy: phân nhóm các phần tử theo key được chỉ định
                // Key = Staff.StaffRole (Admin, Consultant, Accountant,...)
                // Value = danh sách Staff cùng role
                .collect(Collectors.groupingBy(Staff::getRole));
    }

    // ============================================================
    // QUERY 3: Lọc học viên đăng ký trong năm nay, sắp xếp mới nhất trước
    // ============================================================
    /**
     * [Query 3] Lấy danh sách học viên đã đăng ký trong năm hiện tại,
     * sắp xếp theo ngày đăng ký từ mới nhất đến cũ nhất.
     *
     * @return List<Student> học viên của năm hiện tại
     */
    public List<Student> getStudentsRegisteredThisYear() {
        int currentYear = LocalDate.now().getYear(); // lấy năm hiện tại từ hệ thống
        List<Student> allStudents = studentDAO.findAll(); // lấy toàn bộ học viên từ DB

        return allStudents.stream()
                // Bước 1: Loại bỏ học viên chưa có ngày đăng ký (null)
                .filter(s -> s.getRegistrationDate() != null)
                // Bước 2: Lọc học viên có năm đăng ký đúng bằng năm hiện tại
                .filter(s -> s.getRegistrationDate().getYear() == currentYear)
                // Bước 3: Sắp xếp theo registrationDate giảm dần (mới nhất trước)
                .sorted(Comparator.comparing(Student::getRegistrationDate).reversed())
                // Bước 4: Thu thập thành List
                .collect(Collectors.toList());
    }

    // ============================================================
    // QUERY 4: Gộp danh sách email của Teacher + Staff để gửi thông báo
    // ============================================================
    /**
     * [Query 4] Trích xuất và gộp tất cả email của giáo viên và nhân viên.
     * Dùng để lấy danh sách gửi thông báo hàng loạt.
     *
     * @return List<String> danh sách email, bỏ giá trị null và trùng lặp
     */
    public List<String> getAllStaffAndTeacherEmails() {
        List<Teacher> teachers = teacherDAO.findAll(); // tất cả giáo viên
        List<Staff> staffs = staffDAO.findAll(); // tất cả nhân viên

        // Stream.concat: nối hai stream lại thành một để xử lý song song
        List<String> teacherEmails = teachers.stream()
                .map(Teacher::getEmail) // lấy email từ mỗi Teacher
                .filter(Objects::nonNull) // loại bỏ email null
                .collect(Collectors.toList());

        List<String> staffEmails = staffs.stream()
                .map(Staff::getEmail) // lấy email từ mỗi Staff
                .filter(Objects::nonNull) // loại bỏ email null
                .collect(Collectors.toList());

        // Kết hợp hai danh sách, loại bỏ trùng lặp bằng distinct(), sắp xếp A-Z
        return Stream.concat(teacherEmails.stream(), staffEmails.stream())
                .distinct() // loại email trùng
                .sorted() // sắp xếp tăng dần theo alphabet
                .collect(Collectors.toList());
    }

    // ============================================================
    // QUERY 5: Đếm số tài khoản đang bị khoá (is_active = false)
    // ============================================================
    /**
     * [Query 5] Đếm tổng số tài khoản UserAccount đang bị khoá (isActive = false).
     *
     * @return long số lượng tài khoản bị khoá
     */
    public long countLockedAccounts() {
        List<UserAccount> allAccounts = userDAO.findAll(); // lấy tất cả tài khoản

        return allAccounts.stream()
                // Bước 1: Lọc các tài khoản có isActive = false (đang bị khoá)
                .filter(u -> Boolean.FALSE.equals(u.getIsActive()))
                // Bước 2: count() đếm số lượng phần tử còn lại sau khi filter
                .count();
    }

    // ============================================================
    // QUERY 6: Tìm giáo viên có thời gian làm việc lâu nhất (hire_date sớm nhất)
    // ============================================================
    /**
     * [Query 6] Tìm giáo viên có ngày ký hợp đồng (hire_date) sớm nhất,
     * tức là người làm việc lâu năm nhất tại trung tâm.
     *
     * @return Optional<Teacher> giáo viên lâu năm nhất (empty nếu không có dữ liệu)
     */
    public Optional<Teacher> getMostSeniorTeacher() {
        List<Teacher> allTeachers = teacherDAO.findAll(); // lấy tất cả giáo viên

        return allTeachers.stream()
                // Bước 1: Loại bỏ giáo viên chưa có ngày thuê (null)
                .filter(t -> t.getHireDate() != null)
                // Bước 2: min() tìm phần tử có hire_date nhỏ nhất (sớm nhất = lâu năm nhất)
                // Comparator.comparing chỉ định tiêu chí so sánh
                .min(Comparator.comparing(Teacher::getHireDate));
    }

    // ============================================================
    // QUERY 7: Lấy 5 thông báo gần nhất phù hợp với role đăng nhập
    // ============================================================
    /**
     * [Query 7] Lấy tối đa 5 thông báo gần nhất dành cho "All" hoặc đúng role người
     * dùng.
     * Dùng để hiển thị trên Dashboard sau khi đăng nhập.
     *
     * @param currentUserRole role của user đang đăng nhập (lấy từ UserAccount)
     * @return List<Notification> tối đa 5 thông báo phù hợp
     */
    public List<Notification> getRecentNotificationsForUser(UserAccount.UserRole currentUserRole) {
        List<Notification> allNotifications = notiDAO.findRecent(50); // lấy tất cả thông báo (mới nhất trước)

        // Chuyển đổi UserRole -> TargetRole để so sánh với cột target_role trong
        // notifications
        // Ví dụ: UserRole.Teacher -> TargetRole.Teacher
        Notification.TargetRole targetRole;
        try {
            // Thử chuyển UserRole sang TargetRole cùng tên
            targetRole = Notification.TargetRole.valueOf(currentUserRole.name());
        } catch (IllegalArgumentException e) {
            // Nếu không map được (ví dụ role lạ), mặc định dùng "All"
            targetRole = Notification.TargetRole.All;
        }

        final Notification.TargetRole finalRole = targetRole; // cần final để dùng trong lambda

        return allNotifications.stream()
                // Bước 1: Giữ lại thông báo dành cho "All" HOẶC đúng role hiện tại
                .filter(n -> n.getTargetRole() == Notification.TargetRole.All
                        || n.getTargetRole() == finalRole)
                // Bước 2: Sắp xếp theo thời gian tạo mới nhất trước
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                // Bước 3: Giới hạn tối đa 5 kết quả đầu tiên
                .limit(5)
                // Bước 4: Thu thập thành List
                .collect(Collectors.toList());
    }
}
