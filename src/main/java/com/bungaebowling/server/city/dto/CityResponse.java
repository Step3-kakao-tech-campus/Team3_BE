package com.bungaebowling.server.city.dto;

import com.bungaebowling.server.city.City;

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
}