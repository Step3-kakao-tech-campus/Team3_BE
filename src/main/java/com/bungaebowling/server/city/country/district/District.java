package com.bungaebowling.server.city.country.district;

import com.bungaebowling.server.city.country.Country;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "district_tb")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long statutoryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    private Country country;

    @Column(length = 50, nullable = false)
    private String name;

    @Builder
    public District(Long id, Long statutoryCode, Country country, String name) {
        this.id = id;
        this.statutoryCode = statutoryCode;
        this.country = country;
        this.name = name;
    }
}