package com.bungaebowling.server.city.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.city.City;
import com.bungaebowling.server.city.country.Country;
import com.bungaebowling.server.city.dto.CityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cities")
public class CityController {

    @GetMapping
    public ResponseEntity<?> getCities() {
        List<City> cityList = List.of(
                new City(1L, "서울특별시"),
                new City(2L, "부산광역시"),
                new City(3L, "경상북도")
        );

        var getCitiesDto = CityResponse.GetCitiesDto.of(cityList);

        var response = ApiUtils.success(getCitiesDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{cityId}/countries")
    public ResponseEntity<?> getCountries(@PathVariable String cityId) {
        var city = new City(1L, "서울특별시");

        List<Country> countryList = List.of(
                new Country(1L, city, "강남구"),
                new Country(2L, city, "강서구"),
                new Country(3L, city, "종로구"),
                new Country(4L, city, "노원구"),
                new Country(5L, city, "중랑구")
        );

        var getCountriesDto = CityResponse.GetCountriesDto.of(countryList);

        var response = ApiUtils.success(getCountriesDto);
        return ResponseEntity.ok().body(response);
    }
}
