package com.bungaebowling.server.userRate;

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
@Table(name="user_rate_tb")
public class UserRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int starCount;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    private Applicant applicant;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    @Builder
    public UserRate(Long id, int starCount, User user, Applicant applicant, LocalDateTime createdAt) {
        this.id = id;
        this.starCount = starCount;
        this.user = user;
        this.applicant = applicant;
        this.createdAt = createdAt;
    }
}
