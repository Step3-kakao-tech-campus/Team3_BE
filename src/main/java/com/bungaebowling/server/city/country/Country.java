package com.bungaebowling.server.city.country;


import com.bungaebowling.server.city.City;
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
@Table(name = "country_tb", uniqueConstraints = {@UniqueConstraint(columnNames = {"city_id", "name"})})
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private City city;

    @Column(length = 50, nullable = false)
    private String name;

    @Builder
    public Country(Long id, City city, String name) {
        this.id = id;
        this.city = city;
        this.name = name;
    }
}