package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Branch;
import vn.edu.ute.languagecenter.management.repo.BranchRepository;

/** JPA triển khai BranchRepository. */
public class JpaBranchRepository extends GenericRepository<Branch>
        implements BranchRepository {

    public JpaBranchRepository() {
        super(Branch.class);
    }
}
