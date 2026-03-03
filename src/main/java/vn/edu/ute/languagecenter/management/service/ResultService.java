package vn.edu.ute.languagecenter.management.service;

import vn.edu.ute.languagecenter.management.model.Class_;
import vn.edu.ute.languagecenter.management.model.Result;
import vn.edu.ute.languagecenter.management.model.Student;
import vn.edu.ute.languagecenter.management.repo.ResultRepository;
import vn.edu.ute.languagecenter.management.repo.jpa.JpaResultRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class ResultService {

    private final ResultRepository resultRepo = new JpaResultRepository();

    /**
     * Lưu/cập nhật điểm hàng loạt từ JTable.
     * Stream pipeline: forEach scores → findByStudentAndClass (update hoặc tạo mới)
     *   → tính grade tự động → saveAll trong một transaction.
     */
    public List<Result> saveResultsForClass(Class_ class_,
            Map<Student, BigDecimal> scores,
            Map<Student, String> comments) {

        List<Result> toSave = new ArrayList<>();
        scores.forEach((student, score) -> {
            Result r = resultRepo.findByStudentAndClass(student, class_)
                                 .orElse(new Result());
            r.setStudent(student);
            r.setClass_(class_);
            r.setScore(score);
            r.setGrade(calculateGrade(score));
            r.setComment(comments != null ? comments.get(student) : null);
            r.setUpdatedAt(LocalDateTime.now());
            if (r.getCreatedAt() == null) r.setCreatedAt(LocalDateTime.now());
            toSave.add(r);
        });
        return resultRepo.saveAll(toSave);
    }

    public Optional<Result> findByStudentAndClass(Student student, Class_ class_) {
        return resultRepo.findByStudentAndClass(student, class_);
    }

    public List<Result> findByClass(Class_ class_) {
        return resultRepo.findByClass(class_);
    }

    public List<Result> findByStudent(Student student) {
        return resultRepo.findByStudent(student);
    }

    /**
     * Xếp loại tự động dựa trên điểm số.
     * Thang điểm: A+(≥90), A(≥85), B+(≥80), B(≥75), C+(≥70), C(≥65), D(≥50), F(<50).
     */
    public String calculateGrade(BigDecimal score) {
        if (score == null) return "N/A";
        double s = score.doubleValue();
        if (s >= 90) return "A+";
        if (s >= 85) return "A";
        if (s >= 80) return "B+";
        if (s >= 75) return "B";
        if (s >= 70) return "C+";
        if (s >= 65) return "C";
        if (s >= 50) return "D";
        return "F";
    }

    /**
     * Thống kê số lượng từng Grade trong một lớp.
     * Stream pipeline: filter non-null grade → groupingBy(grade) + counting().
     * Kết quả ví dụ: {"A": 3, "B+": 5, "F": 1}
     */
    public Map<String, Long> countByGrade(Class_ class_) {
        return resultRepo.findByClass(class_).stream()
            .filter(r -> r.getGrade() != null)
            .collect(Collectors.groupingBy(
                Result::getGrade,
                Collectors.counting()
            ));
    }

    /**
     * Tính điểm trung bình của cả lớp.
     * Stream pipeline: filter non-null score → mapToDouble → average().
     */
    public OptionalDouble averageScore(Class_ class_) {
        return resultRepo.findByClass(class_).stream()
            .filter(r -> r.getScore() != null)
            .mapToDouble(r -> r.getScore().doubleValue())
            .average();
    }
}
