package vn.edu.ute.languagecenter.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private Level level;

    @Column(name = "duration")
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit")
    private DurationUnit durationUnit = DurationUnit.Week;

    @Column(name = "fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActiveStatus status = ActiveStatus.Active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Relations
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<Class_> classes;

    public enum Level {
        Beginner, Intermediate, Advanced
    }

    public enum DurationUnit {
        Hour, Week
    }

    public enum ActiveStatus {
        Active, Inactive
    }
}
