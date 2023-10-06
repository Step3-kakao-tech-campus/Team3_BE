package com.bungaebowling.server.message;
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
@Table(name = "message_tb")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_user_id", updatable = false)
    private User opponentUser;

    @Column(name = "is_receive", nullable = false)
    private Boolean isReceive;

    @Column(name = "is_read")
    @ColumnDefault(value = "false")
    private Boolean isRead;

    @Column(name = "content",columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    @Builder
    public Message(User user, User opponentUser, Boolean isReceive, Boolean isRead, String content, LocalDateTime createdAt) {
        this.user=user;
        this.opponentUser = opponentUser;
        this.isReceive = isReceive;
        this.isRead = isRead;
        this.content = content;
        this.createdAt = createdAt;
    }
    public void read(){
        this.isRead = true;
    }
}
