package vn.edu.ute.languagecenter.management.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.languagecenter.management.model.UserAccount;
import vn.edu.ute.languagecenter.management.repo.UserAccountRepository;

import java.util.List;
import java.util.Optional;

/** JPA triển khai UserAccountRepository — xác thực đăng nhập, phân quyền. */
public class JpaUserAccountRepository extends GenericRepository<UserAccount>
        implements UserAccountRepository {

    public JpaUserAccountRepository() {
        super(UserAccount.class);
    }

    @Override
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

    @Override
    public Optional<UserAccount> login(String username, String password) {
        EntityManager em = getEntityManager();
        try {
            // Dùng LEFT JOIN FETCH để eager-load teacher/staff/student
            // tránh LazyInitializationException sau khi em.close()
            String hql = "SELECT u FROM UserAccount u " +
                    "LEFT JOIN FETCH u.teacher " +
                    "LEFT JOIN FETCH u.staff " +
                    "LEFT JOIN FETCH u.student " +
                    "WHERE u.username = :username " +
                    "  AND u.isActive = true";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            q.setParameter("username", username);
            List<UserAccount> list = q.getResultList();
            
            if (list.isEmpty()) {
                return Optional.empty();
            }
            
            UserAccount u = list.get(0);
            String dbHash = u.getPasswordHash();
            
            // Fallback cho tài khoản còn đang dùng mật khẩu plain text cũ
            if (dbHash != null && (dbHash.startsWith("$2a$") || dbHash.startsWith("$2b$") || dbHash.startsWith("$2y$"))) {
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, dbHash)) {
                    return Optional.of(u);
                }
            } else {
                if (password.equals(dbHash)) {
                    return Optional.of(u);
                }
            }
            
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
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

    @Override
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

    @Override
    public List<UserAccount> findAllWithLinks() {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT u FROM UserAccount u " +
                    "LEFT JOIN FETCH u.teacher " +
                    "LEFT JOIN FETCH u.staff " +
                    "LEFT JOIN FETCH u.student " +
                    "ORDER BY u.username ASC";
            TypedQuery<UserAccount> q = em.createQuery(hql, UserAccount.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
