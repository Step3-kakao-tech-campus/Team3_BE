package com.bungaebowling.server.post;

import com.bungaebowling.server.applicant.Applicant;
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
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "due_time", nullable = false)
    private LocalDateTime dueTime;

    @Column(name = "is_close")
    @ColumnDefault(value = "false")
    private Boolean isClose;

    @Column(name = "view_count")
    @ColumnDefault(value = "0")
    private int viewCount;

    @Column(name = "edited_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime editedAt;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault(value = "now()")
    private LocalDateTime createdAt;

    //@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    //private final List<Comment> comments = new ArrayList<>();

    //@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    //private final List<Score> scores = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private final List<Applicant> applicants = new ArrayList<>();

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
                this.district.getCountry().getName() + " "  +
                this.district.getName();
    }


    public int getApplicantNumber() { // 현재 신청한 사람 수
        return applicants.size();
    }

    public int getCurrentNumber() { // 현재 모집된 사람 수
        int count = 0;

        for (Applicant applicant : applicants) {
            if (applicant.getStatus()) {
                count++;
            }
        }

        return count;
    }


    public void addViewCount() { // viewCount 증가
        this.viewCount++;
    }

    public Boolean isMine(Long userId) { // 내가 작성한 글인지 아닌지 확인
        return this.user.getId().equals(userId);
    }

    public void update(String newTitle, String newContent, LocalDateTime newStartTime, LocalDateTime newDueTime, Boolean newIsClose) { // 게시글 업데이트할 때 쓸 것
        this.title = newTitle;
        this.content = newContent;
        this.startTime = newStartTime;
        this.dueTime = newDueTime;
        this.isClose = newIsClose;
    }

    public String getProfilePath() { // 사용자 Profile 이미지 경로 가져오기
        return this.user.getImgUrl();
    }

}