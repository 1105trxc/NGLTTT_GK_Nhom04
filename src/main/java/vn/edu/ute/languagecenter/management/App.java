package vn.edu.ute.languagecenter.management;

import vn.edu.ute.languagecenter.management.gui.operation.LoginForm;
import vn.edu.ute.languagecenter.management.gui.operation.MainDashboard;
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
    private static final boolean DEV_MODE = true;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            if (DEV_MODE) {
                // Tạo user Admin giả để test nhanh, không cần DB / đăng nhập
                UserAccount devAdmin = new UserAccount();
                devAdmin.setUsername("admin");
                devAdmin.setRole(UserAccount.UserRole.Admin);
                devAdmin.setIsActive(true);
                new MainDashboard(devAdmin).setVisible(true);
            } else {
                // Luồng thật: hiện LoginForm, kết nối DB
                new LoginForm().setVisible(true);
            }
        });
    }
}
