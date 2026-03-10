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
    public Optional<UserAccount> login(String username, String passwordHash) {
        EntityManager em = getEntityManager();
        try {
            // Dùng LEFT JOIN FETCH để eager-load teacher/staff/student
            // tránh LazyInitializationException sau khi em.close()
            String hql = "SELECT u FROM UserAccount u " +
                    "LEFT JOIN FETCH u.teacher " +
                    "LEFT JOIN FETCH u.staff " +
                    "LEFT JOIN FETCH u.student " +
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
