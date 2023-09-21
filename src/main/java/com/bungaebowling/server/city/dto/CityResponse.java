package com.bungaebowling.server.city.dto;

import com.bungaebowling.server.city.City;
import com.bungaebowling.server.city.country.Country;

import java.util.List;

public class CityResponse {
    public record GetCitiesDto(
            List<CityDto> cities
    )  {
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
    )  {
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
}