package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Staff;

import java.util.List;
import java.util.Optional;

/** Giao diện Repository cho Staff. */
public interface StaffRepository {
    void save(Staff staff);

    Staff update(Staff staff);

    void delete(Object id);

    Optional<Staff> findById(Object id);

    List<Staff> findAll();
}
