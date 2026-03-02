package vn.edu.ute.languagecenter.management.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.UserAccount;

import java.util.List;
import java.util.Optional;

/**
 * UserAccountDAO - DAO chuyên biệt cho entity UserAccount.
 * Xử lý các truy vấn liên quan đến tài khoản người dùng và phân quyền.
 */
public class UserAccountDAO extends GenericDAO<UserAccount> {

    public UserAccountDAO() {
        super(UserAccount.class);
    }

    /**
     * Tìm tài khoản theo tên đăng nhập (dùng cho chức năng Login).
     * 
     * @param username tên đăng nhập
     * @return Optional<UserAccount> - có hoặc không có kết quả
     */
    public Optional<UserAccount> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT u FROM UserAccount u WHERE u.username = :username";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            q.setParameter("username", username);
            List<UserAccount> result = q.getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Xác thực đăng nhập: so khớp username và password.
     * 
     * @param username     tên đăng nhập
     * @param passwordHash mật khẩu đã băm (hoặc plain-text nếu chưa hash)
     * @return Optional<UserAccount> nếu thông tin đăng nhập hợp lệ
     */
    public Optional<UserAccount> login(String username, String passwordHash) {
        EntityManager em = getEntityManager();
        try {
            // Tìm user đang active, khớp cả username lẫn passwordHash
            String hql = "SELECT u FROM UserAccount u " +
                    "WHERE u.username = :username " +
                    "  AND u.passwordHash = :pwd " +
                    "  AND u.isActive = true";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            q.setParameter("username", username);
            q.setParameter("pwd", passwordHash);
            List<UserAccount> list = q.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả tài khoản theo role (phân lọc để quản lý).
     * 
     * @param role Admin / Teacher / Student / Staff
     * @return danh sách tài khoản theo role đó
     */
    public List<UserAccount> findByRole(UserAccount.UserRole role) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT u FROM UserAccount u WHERE u.role = :role ORDER BY u.username ASC";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            q.setParameter("role", role);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả tài khoản bị khoá (is_active = false).
     * 
     * @return danh sách tài khoản bị khoá
     */
    public List<UserAccount> findAllInactive() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT u FROM UserAccount u WHERE u.isActive = false ORDER BY u.username ASC";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
