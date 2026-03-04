package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Branch;

import java.util.List;
import java.util.Optional;

/** Giao diện Repository cho Branch. */
public interface BranchRepository {
    void save(Branch branch);

    Branch update(Branch branch);

    void delete(Object id);

    Optional<Branch> findById(Object id);

    List<Branch> findAll();
}
