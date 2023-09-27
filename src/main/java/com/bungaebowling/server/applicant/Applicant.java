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
@Table(name="applicant_tb")
//@Table(name="applicant_tb", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"user_id", "post_id"})
//})
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("false")
    private Boolean status;

    private Long userId; //임시

//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;

    //@OneToOne
    //private UserRate userRate;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    @Builder
    public Applicant(Long id, Boolean status, Long userId, Post post, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.userId = userId;
        this.post = post;
        this.createdAt = createdAt;
    }
}
