package com.bungaebowling.server.city.dto;

import com.bungaebowling.server.city.City;
import com.bungaebowling.server.city.country.Country;
import com.bungaebowling.server.city.country.district.District;

import java.util.List;

public class CityResponse {
    public record GetCitiesDto(
            List<CityDto> cities
    ) {
        public static GetCitiesDto of(List<City> cityList) {
            return new GetCitiesDto(cityList.stream()
                    .map(CityDto::new)
                    .toList());
        }

        public record CityDto(
                Long id,
                String name
        ) {
            CityDto(City city) {
                this(city.getId(), city.getName());
            }
        }
    }

    public record GetCountriesDto(
            List<CountryDto> countries
    ) {
        public static GetCountriesDto of(List<Country> countryList) {
            return new GetCountriesDto(countryList.stream()
                    .map(CountryDto::new)
                    .toList());
        }

        public record CountryDto(
                Long id,
                String name
        ) {
            CountryDto(Country country) {
                this(country.getId(), country.getName());
            }
        }
    }

    public record GetDistrictsDto(
            List<DistrictDto> districts
    ) {
        public static GetDistrictsDto of(List<District> districtList) {
            return new GetDistrictsDto(districtList.stream()
                    .map(DistrictDto::new)
                    .toList());
        }

        public record DistrictDto(
                Long id,
                String name
        ) {
            DistrictDto(District district) {
                this(district.getId(), district.getName());
            }
        }
    }

    public record GetDistrictInfoDto(
            Long cityId,
            String cityName,
            Long countryId,
            String countryName,
            String name
    ) {

        public GetDistrictInfoDto(District district) {
            this(
                    district.getCountry().getCity().getId(),
                    district.getCountry().getCity().getName(),
                    district.getCountry().getId(),
                    district.getCountry().getName(),
                    district.getName()
            );
        }
    }
}