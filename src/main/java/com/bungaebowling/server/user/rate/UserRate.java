package com.bungaebowling.server.user.rate;

import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_rate_tb")
public class UserRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @Column(nullable = false)
    private Integer starCount;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    @Builder
    public UserRate(Long id, User user, Applicant applicant, Integer starCount, LocalDateTime createdAt) {
        this.id = id;
        this.starCount = starCount;
        this.user = user;
        this.applicant = applicant;
        this.createdAt = createdAt;
    }
}
