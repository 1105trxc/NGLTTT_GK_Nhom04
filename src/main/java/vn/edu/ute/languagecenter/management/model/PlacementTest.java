package vn.edu.ute.languagecenter.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "placement_tests")
public class PlacementTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "suggested_level", length = 100)
    private String suggestedLevel;

    @Column(name = "note", length = 255)
    private String note;

}