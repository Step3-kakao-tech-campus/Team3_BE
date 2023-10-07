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

    // Todo: 유효성 검사 추가해주기
    @Column(nullable = false)
    private Integer scoreNum;

    @Column(name = "result_image_url", nullable = false)
    private String resultImageUrl;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    @Builder
    public Score(User user, Post post, Integer scoreNum, String resultImageUrl, LocalDateTime createdAt) {
        this.user = user;
        this.post = post;
        this.scoreNum = scoreNum;
        this.resultImageUrl = resultImageUrl;
        this.createdAt = createdAt;
    }

    public void update(User user, Post post, Integer scoreNum, String resultImageUrl, LocalDateTime updatedAt){
        this.user = user;
        this.post = post;
        this.scoreNum = scoreNum;
        this.resultImageUrl = resultImageUrl;
        this.createdAt = updatedAt;
    }
}
