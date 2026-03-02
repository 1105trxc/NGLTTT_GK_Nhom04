package vn.edu.ute.languagecenter.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "placement_tests")
public class PlacementTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    // Thêm mới: đổi từ VARCHAR(100) → ENUM('Beginner','Intermediate','Advanced')
    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_level")
    private SuggestedLevel suggestedLevel;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum SuggestedLevel { Beginner, Intermediate, Advanced }
}
