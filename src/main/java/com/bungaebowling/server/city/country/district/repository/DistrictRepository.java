package com.bungaebowling.server.city.country.district.repository;

import com.bungaebowling.server.city.country.district.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCountryId(Long countryId);

    @Query("SELECT d FROM District d JOIN FETCH d.country c JOIN FETCH c.city ct WHERE d.id=:id")
    Optional<District> findByIdJoinAll(Long id);
}
