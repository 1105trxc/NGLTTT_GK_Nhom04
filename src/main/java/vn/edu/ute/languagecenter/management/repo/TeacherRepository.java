package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Teacher;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện Repository cho Teacher — định nghĩa contract CRUD + truy vấn đặc
 * thù.
 */
public interface TeacherRepository {
    void save(Teacher teacher);

    Teacher update(Teacher teacher);

    void delete(Object id);

    Optional<Teacher> findById(Object id);

    List<Teacher> findAll();

    List<Teacher> findByStatus(Teacher.ActiveStatus status);

    Optional<Teacher> findByEmail(String email);

    List<Teacher> findBySpecialty(String specialty);

    List<Teacher> findAllActive();
}
