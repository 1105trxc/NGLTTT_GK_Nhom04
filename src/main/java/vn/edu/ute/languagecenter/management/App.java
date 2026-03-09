package vn.edu.ute.languagecenter.management;

import vn.edu.ute.languagecenter.management.ui.LoginForm;
import vn.edu.ute.languagecenter.management.ui.MainDashboard;
import vn.edu.ute.languagecenter.management.model.UserAccount;

import javax.swing.*;

/**
 * Điểm khởi động của ứng dụng.
 *
 * DEV_MODE = true → bỏ qua login, vào thẳng Dashboard với quyền Admin.
 * DEV_MODE = false → hiện màn hình đăng nhập bình thường.
 */
public class App {

    // *** ĐỔI THÀNH false KHI NỘP BÀI / DEMO THẬT ***
    private static final boolean DEV_MODE = false;

    // Nếu DEV_MODE = true, bạn được chọn quyền để test nhanh (không cần đăng nhập).
    // Các giá trị có thể chọn:
    // - UserAccount.UserRole.Admin
    // - UserAccount.UserRole.Staff
    // - UserAccount.UserRole.Teacher
    // - UserAccount.UserRole.Student
    private static final UserAccount.UserRole DEV_ROLE = UserAccount.UserRole.Admin; // Đổi quyền ở đây nhe!

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            if (DEV_MODE) {
                // Tạo user giả để test nhanh, không cần DB / đăng nhập
                UserAccount devUser = new UserAccount();
                devUser.setUsername(DEV_ROLE.name().toLowerCase() + "_test");
                devUser.setRole(DEV_ROLE);
                devUser.setIsActive(true);
                new MainDashboard(devUser).setVisible(true);
            } else {
                // Luồng thật: hiện LoginForm, kết nối DB
                new LoginForm().setVisible(true);
            }
        });
    }
}
