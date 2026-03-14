package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.UserAccount;

import java.util.List;
import java.util.Optional;

/** Giao diện Repository cho UserAccount — login, phân quyền. */
public interface UserAccountRepository {
    void save(UserAccount account);

    UserAccount update(UserAccount account);

    void delete(Object id);

    Optional<UserAccount> findById(Object id);

    List<UserAccount> findAll();

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> login(String username, String password);

    List<UserAccount> findByRole(UserAccount.UserRole role);

    List<UserAccount> findAllInactive();

    List<UserAccount> findAllWithLinks();
}
