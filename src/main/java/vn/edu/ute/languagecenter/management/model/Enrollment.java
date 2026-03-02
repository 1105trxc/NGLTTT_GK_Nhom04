package vn.edu.ute.languagecenter.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(name = "uq_enrollments_student_class", columnNames = {"student_id", "class_id"})
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class_ class_;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.Enrolled;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private ResultStatus result = ResultStatus.NA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "enrollment", fetch = FetchType.LAZY)
    private List<Payment> payments;

    public enum EnrollmentStatus { Enrolled, Dropped, Completed }
    public enum ResultStatus { Pass, Fail, NA }
}
