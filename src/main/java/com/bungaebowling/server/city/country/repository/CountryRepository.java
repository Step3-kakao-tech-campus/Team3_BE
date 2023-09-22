package com.bungaebowling.server.city.country.repository;

import com.bungaebowling.server.city.country.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByCityId(Long cityId);
}
