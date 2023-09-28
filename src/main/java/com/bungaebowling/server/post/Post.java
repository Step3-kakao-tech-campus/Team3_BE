package com.bungaebowling.server.post;

import com.bungaebowling.server.Score.Score;
import com.bungaebowling.server.city.country.Country;
import com.bungaebowling.server.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_tb")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", updatable = false) // updatable = false -> 작성자 변경 불가
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "district_id")
    private Country country;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @Column(name = "is_close")
    private Boolean isClose;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "edited_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime editedAt;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    //@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    //private final List<Comment> comments = new ArrayList<>();

    //@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    //private final List<Score> scores = new ArrayList<>();

    //@OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    //private final List<Applicant> applicants = new ArrayList<>();

    @Builder
    public Post(String title, User user, Country country, String content, LocalDateTime startTime, LocalDateTime dueTime, Boolean isClose, int viewCount, LocalDateTime editedAt, LocalDateTime createdAt) {
        this.title = title;
        this.user = user;
        this.country = country;
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
        //return this.country.getName();
        return "부산광역시 금정구 장전2동"; // 임시 결과
    }


    public int getApplicantNumber() { // 현재 신청한 사람 수
        //return applicants.size();
        return 0; // 임시
    }

    public int getCurrentNumber() { // 현재 모집된 사람 수

        int count = 0;

        /*
        for (Applicant applicant : applicants) {
            if (applicant.isStatus()) {
                count++;
            }
        }*/

        count++; // 임시

        return count;
    }


    public void addViewCount() { // viewCount 증가
        this.viewCount++;
    }

    public Boolean isMine(User user) { // 내가 작성한 글인지 아닌지 확인
        return this.user.getId().equals(user.getId());
    }

    public void update(Post post) { // 게시글 업데이트할 때 쓸 것
        this.title = post.getTitle();
        this.content = post.getContent();
        this.startTime = post.getDueTime();
        this.dueTime = post.getDueTime();
        this.isClose = post.isClose;
    }

    public String getProfilePath() { // 사용자 Profile 이미지 경로 가져오기
        return Optional.ofNullable(this.user.getImgUrl()).orElse(null);
    }

}