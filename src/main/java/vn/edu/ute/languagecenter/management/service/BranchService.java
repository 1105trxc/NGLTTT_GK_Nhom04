package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Branch;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaBranchRepository;

import java.util.List;
import java.util.Optional;

public class BranchService {
    private final JpaBranchRepository branchRepository = new JpaBranchRepository();

    public List<Branch> findAll() {
        return branchRepository.findAll();
    }

    public List<Branch> findAllActive() {
        return branchRepository.findAll().stream()
                .filter(b -> b.getStatus() == Branch.ActiveStatus.Active)
                .toList();
    }

    public Optional<Branch> findById(Long id) {
        return branchRepository.findById(id);
    }

    public void save(Branch branch) {
        branchRepository.save(branch);
    }

    public void update(Branch branch) {
        branchRepository.update(branch);
    }

    public void deleteById(Long id) {
        branchRepository.delete(id);
    }

    public List<Branch> findByName(String name) {
        String kw = name.toLowerCase();
        return branchRepository.findAll().stream()
                .filter(b -> b.getBranchName().toLowerCase().contains(kw))
                .toList();
    }
}
