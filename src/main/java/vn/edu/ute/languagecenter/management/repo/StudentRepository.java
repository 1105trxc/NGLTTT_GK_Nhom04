package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Student;

import java.util.List;
import java.util.Optional;

/** Giao diện Repository cho Student. */
public interface StudentRepository {
    void save(Student student);

    Student update(Student student);

    void delete(Object id);

    Optional<Student> findById(Object id);

    List<Student> findAll();
}
