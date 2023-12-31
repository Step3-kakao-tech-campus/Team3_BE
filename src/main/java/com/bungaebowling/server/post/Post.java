package com.bungaebowling.server.post;

import com.bungaebowling.server.city.country.district.District;
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
@Table(name = "post_tb")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false) // updatable = false -> 작성자 변경 불가
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime dueTime;

    @ColumnDefault(value = "false")
    private Boolean isClose;

    @ColumnDefault(value = "0")
    private int viewCount;

    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime editedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    @Builder
    public Post(String title, User user, District district, String content, LocalDateTime startTime, LocalDateTime dueTime, Boolean isClose, int viewCount, LocalDateTime editedAt, LocalDateTime createdAt) {
        this.title = title;
        this.user = user;
        this.district = district;
        this.content = content;
        this.startTime = startTime;
        this.dueTime = dueTime;
        this.isClose = isClose;
        this.viewCount = viewCount;
        this.editedAt = editedAt;
        this.createdAt = createdAt;
    }

    public String getUserName() { // 사용자 이름 가져오기
        return this.user.getName();
    }

    public String getDistrictName() {
        return this.district.getCountry().getCity().getName() + " " +
                this.district.getCountry().getName() + " " +
                this.district.getName();
    }

    public void addViewCount() { // viewCount 증가
        this.viewCount++;
    }

    public Boolean isMine(Long userId) { // 내가 작성한 글인지 아닌지 확인
        return this.user.getId().equals(userId);
    }

    public void update(String newTitle, String newContent, LocalDateTime newStartTime, LocalDateTime newDueTime, LocalDateTime editedAt) { // 게시글 업데이트할 때 쓸 것
        this.title = newTitle;
        this.content = newContent;
        this.startTime = newStartTime;
        this.dueTime = newDueTime;
        this.editedAt = editedAt;
    }

    public String getProfilePath() { // 사용자 Profile 이미지 경로 가져오기
        return this.user.getImgUrl();
    }

    public void updateIsClose(boolean isClose) {
        this.isClose = isClose;
    }

}