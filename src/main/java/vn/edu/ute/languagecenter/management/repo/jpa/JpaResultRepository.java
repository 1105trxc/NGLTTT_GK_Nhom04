package vn.edu.ute.languagecenter.management.repo.jpa;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Result;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.db.TransactionManager;
import vn.edu.ute.languagecenter.management.repo.ResultRepository;
import java.util.List;
import java.util.Optional;

public class JpaResultRepository implements ResultRepository {

    private final TransactionManager tm = new TransactionManager();

    @Override
    public Result save(Result result) {
        try {
            return tm.runInTransaction(em -> {
                if (result.getResultId() == null) {
                    em.persist(result);
                    return result;
                }
                return em.merge(result);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu Result", e);
        }
    }

    @Override
    public List<Result> saveAll(List<Result> list) {
        try {
            return tm.runInTransaction(em -> {
                list.forEach(r -> {
                    if (r.getResultId() == null) em.persist(r);
                    else em.merge(r);
                });
                return list;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi saveAll Result", e);
        }
    }

    @Override
    public Optional<Result> findById(Long id) {
        try {
            return tm.runInTransaction(em ->
                    Optional.ofNullable(em.find(Result.class, id))
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findById Result", e);
        }
    }

    @Override
    public List<Result> findByClass(Class_ class_) {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT r FROM Result r JOIN FETCH r.student " +
                                    "WHERE r.class_ = :c ORDER BY r.student.fullName",
                            Result.class
                    ).setParameter("c", class_).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByClass Result", e);
        }
    }

    @Override
    public Optional<Result> findByStudentAndClass(Student student, Class_ class_) {
        return findByClass(class_).stream()
                .filter(r -> r.getStudent().getStudentId()
                        .equals(student.getStudentId()))
                .findFirst();
    }

    @Override
    public List<Result> findByStudent(Student student) {
        try {
            return tm.runInTransaction(em ->
                    em.createQuery(
                            "SELECT r FROM Result r JOIN FETCH r.class_ WHERE r.student = :s",
                            Result.class
                    ).setParameter("s", student).getResultList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi findByStudent Result", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            tm.runInTransaction(em -> {
                Result r = em.find(Result.class, id);
                if (r != null) em.remove(r);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa Result", e);
        }
    }
}