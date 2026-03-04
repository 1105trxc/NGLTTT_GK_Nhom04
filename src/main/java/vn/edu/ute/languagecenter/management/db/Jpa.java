package vn.edu.ute.languagecenter.management.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Quản lý kết nối JPA (Singleton pattern).
 * Tạo EntityManagerFactory một lần duy nhất khi ứng dụng khởi chạy,
 * cung cấp EntityManager cho mỗi thao tác DB.
 */
public final class Jpa {

    // EntityManagerFactory duy nhất, đọc cấu hình từ persistence.xml
    // Tên "languageCenterPU" phải khớp chính xác với name trong persistence.xml
    private static EntityManagerFactory emf;

    private Jpa() {
    } // Không cho phép tạo instance

    /** Tạo EntityManager mới — mỗi thao tác DB nên dùng 1 em riêng */
    public static EntityManager em() {
        if (emf == null) {
            try {
                emf = Persistence.createEntityManagerFactory("languageCenterPU");
            } catch (Exception e) {
                System.err.println("Cannot init EntityManagerFactory");
                e.printStackTrace();
                throw e;
            }
        }
        return emf.createEntityManager();
    }

    /** Đóng kết nối khi tắt ứng dụng */
    public static void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }
}