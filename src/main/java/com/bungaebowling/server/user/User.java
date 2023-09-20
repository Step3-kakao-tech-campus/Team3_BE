package com.bungaebowling.server.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

//    @ManyToOne(fetch = FetchType.LAZY)
//    private District district;

    @Column(nullable = false)
    private Long district_id; // District 만들고 District로 바꿔야함

    @Column(length = 200)
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ROLE_PENDING'")
    @Column(length = 50)
    private Role role;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    @Builder
    public User(Long id, String name, String email, String password, Long district_id, String imgUrl, Role role, LocalDateTime created_at) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.district_id = district_id;
        this.imgUrl = imgUrl;
        this.role = role;
        this.createdAt = created_at;
    }
}
