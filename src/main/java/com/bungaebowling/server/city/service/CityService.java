package com.bungaebowling.server.city.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server.city.City;
import com.bungaebowling.server.city.country.Country;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.city.country.repository.CountryRepository;
import com.bungaebowling.server.city.dto.CityResponse;
import com.bungaebowling.server.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CityService {

    final private CityRepository cityRepository;

    final private CountryRepository countryRepository;

    final private DistrictRepository districtRepository;

    public CityResponse.GetCitiesDto getCities() {
        List<City> cities = cityRepository.findAll();

        return CityResponse.GetCitiesDto.of(cities);
    }

    public CityResponse.GetCountriesDto getCountries(Long cityId) {
        List<Country> countries = countryRepository.findByCityId(cityId);

        return CityResponse.GetCountriesDto.of(countries);
    }

    public CityResponse.GetDistrictsDto getDistricts(Long countryId) {
        List<District> districts = districtRepository.findByCountryId(countryId);

        return CityResponse.GetDistrictsDto.of(districts);
    }

    public CityResponse.GetDistrictInfoDto getDistrictInfo(Long districtId) {
        District district = districtRepository.findByIdJoinAll(districtId).orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
        return new CityResponse.GetDistrictInfoDto(district);
    }
}
