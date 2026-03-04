package vn.edu.ute.languagecenter.management.repo;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Result;
import vn.edu.ute.languagecenter.management.model.Student;
import java.util.List;
import java.util.Optional;

public interface ResultRepository {
    Result save(Result result);
    List<Result> saveAll(List<Result> list);
    Optional<Result> findById(Long id);
    Optional<Result> findByStudentAndClass(Student student, Class_ class_);
    List<Result> findByClass(Class_ class_);
    List<Result> findByStudent(Student student);
    void deleteById(Long id);
}
