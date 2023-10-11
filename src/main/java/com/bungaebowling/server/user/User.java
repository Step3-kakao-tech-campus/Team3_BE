package com.bungaebowling.server.user;

import com.bungaebowling.server.city.country.district.District;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_tb")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    private District district;

    @Column(length = 200)
    private String imgUrl;

    private String accessImageUrl;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ROLE_PENDING'")
    @Column(length = 50)
    private Role role;

    @ColumnDefault("now()")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public User(Long id, String name, String email, String password, District district, String imgUrl, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.district = district;
        this.imgUrl = imgUrl;
        this.role = role;
        this.createdAt = createdAt;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateProfile(String name, District district, String imgUrl, String accessImageUrl, LocalDateTime updatedAt){
        this.name = Objects.nonNull(name) ? name : this.name;
        this.district = Objects.nonNull(district) ? district : this.district;
        this.imgUrl = Objects.nonNull(imgUrl) ? imgUrl : this.imgUrl;
        this.accessImageUrl = accessImageUrl;
        this.updatedAt = updatedAt;
    }

    public String getDistrictName() {
        return this.district.getCountry().getCity().getName() + " " +
                this.district.getCountry().getName() + " "  +
                this.district.getName();
    }
}
