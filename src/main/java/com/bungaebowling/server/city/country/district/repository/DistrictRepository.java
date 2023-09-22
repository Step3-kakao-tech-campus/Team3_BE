package com.bungaebowling.server.city.country.district.repository;

import com.bungaebowling.server.city.country.district.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCountryId(Long countryId);
}
