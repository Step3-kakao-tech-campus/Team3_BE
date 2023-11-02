package com.bungaebowling.server.comment;

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
@Table(name = "comment_tb")
public class Comment {

    public static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "edited_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime editedAt;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    @Builder
    public Comment(Long id, Post post, User user, Comment parent, String content, LocalDateTime editedAt, LocalDateTime createdAt) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.parent = parent;
        this.content = content;
        this.editedAt = editedAt;
        this.createdAt = createdAt;
    }

    public void updateContent(String content) {
        this.content = content;
        this.editedAt = LocalDateTime.now();
    }

    public void delete() {
        this.content = DELETED_COMMENT_CONTENT;
        this.user = null;
    }
}
