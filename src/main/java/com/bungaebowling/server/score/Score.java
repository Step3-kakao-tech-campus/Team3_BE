package com.bungaebowling.server.score;

import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "score_tb")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    @Range(min = 0, max = 300, message = "점수는 0~300까지의 수만 입력 가능합니다.")
    private Integer scoreNum;

    private String resultImageUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    // 브라우저 상의 이미지 접근 경로
    private String accessImageUrl;

    @Builder
    public Score(User user, Post post, Integer scoreNum, String resultImageUrl, LocalDateTime createdAt, String accessImageUrl) {
        this.user = user;
        this.post = post;
        this.scoreNum = scoreNum;
        this.resultImageUrl = resultImageUrl;
        this.createdAt = createdAt;
        this.accessImageUrl = accessImageUrl;
    }

    public void updateWithFile(String resultImageUrl, String accessImageUrl) {
        this.resultImageUrl = resultImageUrl;
        this.createdAt = LocalDateTime.now();
        this.accessImageUrl = accessImageUrl;
    }

    public void updateScoreNum(Integer scoreNum) {
        this.scoreNum = scoreNum;
    }
}
