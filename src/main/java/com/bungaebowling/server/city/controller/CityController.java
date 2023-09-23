package com.bungaebowling.server.city.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.city.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cities")
public class CityController {

    final private CityService cityService;

    @GetMapping
    public ResponseEntity<?> getCities() {
        var getCitiesDto = cityService.getCities();

        var response = ApiUtils.success(getCitiesDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{cityId}/countries")
    public ResponseEntity<?> getCountries(@PathVariable Long cityId) {
        var getCountriesDto = cityService.getCountries(cityId);

        var response = ApiUtils.success(getCountriesDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/countries/{countryId}/districts")
    public ResponseEntity<?> getDistricts(@PathVariable Long countryId) {
        var getDistrictsDto = cityService.getDistricts(countryId);

        var response = ApiUtils.success(getDistrictsDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/districts/{districtId}")
    public ResponseEntity<?> getDistrictInfo(@PathVariable Long districtId) {
        var getDistrictInfoDto = cityService.getDistrictInfo(districtId);

        var response = ApiUtils.success(getDistrictInfoDto);
        return ResponseEntity.ok().body(response);
    }
}
