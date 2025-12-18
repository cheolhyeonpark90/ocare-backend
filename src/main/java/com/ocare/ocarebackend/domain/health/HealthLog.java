package com.ocare.ocarebackend.domain.health;

import com.ocare.ocarebackend.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_recordKey_measuredAt", columnList = "recordKey, measuredAt"))
public class HealthLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recordKey;

    @Column(nullable = false)
    private LocalDateTime measuredAt;

    @ColumnDefault("0")
    private Integer steps;

    @ColumnDefault("0.0")
    private Float distance;

    @ColumnDefault("0.0")
    private Float calories;
}
