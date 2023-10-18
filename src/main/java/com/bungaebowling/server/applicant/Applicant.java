package com.bungaebowling.server.applicant;

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
@Table(name="applicant_tb", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("false")
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    @Builder
    public Applicant(Long id, Boolean status, User user, Post post, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.user = user;
        this.post = post;
        this.createdAt = createdAt;
    }

    public void updateStatus(Boolean status){
        this.status = status;
    }
}
