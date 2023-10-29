package com.bungaebowling.server.place.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.place.dto.PlaceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.bungaebowling.server._core.errors.exception.ErrorCode.*;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PlaceService {

    @Value("${google.api.places.key}")
    private String apiKey;

    private final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    private final String PHOTO_API_URL = "https://maps.googleapis.com/maps/api/place/photo";

    private final DistrictRepository districtRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public PlaceResponse.GetPlaceDto getPlaceDetails(String name, Long placeId) {
        String districtName = getDistrictName(placeId);
        String googlePlaceId = getGooglePlaceId(name, districtName);
        String url = createPlaceUrl(googlePlaceId);

        String response = restTemplate.getForObject(url, String.class);
        log.info("detail response=" + response);
        return extractPlaceDetails(response);
    }

    private String getGooglePlaceId(String name, String address) {
        String url = createGooglePlaceIdUrl(name, address);
        log.info("google place id url=" + url);

        String response = restTemplate.getForObject(url, String.class);
        log.info("id response=" + response);
        return extractPlaceId(response);
    }

    private PlaceResponse.GetPlaceDto extractPlaceDetails(String response) {
        try {
            JsonNode result = objectMapper.readTree(response).path("result");
            if (result.isEmpty()) {
                throw new CustomException(PLACE_DETAILS_NOT_FOUND);
            }

            //볼링장 이름, 주소, 전화번호
            String name = result.path("name").asText();
            String address = result.path("formatted_address").asText();
            String phoneNumber = result.path("formatted_phone_number").asText();

            //볼링장 사진
            List<String> images = new ArrayList<>();
            JsonNode photoNode = result.path("photos");
            for (int i = 0; i < photoNode.size(); i++) {
                String photoReference = photoNode.get(i).path("photo_reference").asText();
                String imageUrl = createImageUrl(photoReference);
                images.add(imageUrl);
            }

            //볼링장 영업 시간
            List<String> operationTimes = new ArrayList<>();
            JsonNode weekdayTextNode = result.path("opening_hours").path("weekday_text");
            for (int i = 0; i < weekdayTextNode.size(); i++) {
                String operationTime = weekdayTextNode.get(i).asText();
                operationTimes.add(operationTime);
            }

            return new PlaceResponse.GetPlaceDto(name, images, address, phoneNumber, operationTimes);
        } catch (JsonProcessingException e) {
            throw new CustomException(PLACE_DETAILS_CONVERSION_ERROR);
        }
    }

    private String extractPlaceId(String response) {
        try {
            JsonNode results = objectMapper.readTree(response).path("results");

            if (results.size() == 0) {
                throw new CustomException(PLACE_DETAILS_NOT_FOUND);
            }

            return results.get(0).path("place_id").asText();
        } catch (JsonProcessingException e) {
            throw new CustomException(PLACE_DETAILS_CONVERSION_ERROR);
        }
    }

    private String createPlaceUrl(String placeId) {
        return new StringBuilder(PLACES_API_URL)
                .append("?language=ko")
                .append("&place_id=").append(placeId)
                .append("&key=").append(apiKey)
                .toString();
    }

    private String createGooglePlaceIdUrl(String name, String districtName) {
        StringBuilder addressBuilder = new StringBuilder();

        if (name != null) {
            addressBuilder.append(name);
        }

        if (name != null && districtName != null) {
            addressBuilder.append(",");
        }

        if (districtName != null) {
            addressBuilder.append(districtName);
        }

        return new StringBuilder(GEOCODING_API_URL)
                .append("?language=ko")
                .append("&address=").append(addressBuilder)
                .append("&key=").append(apiKey)
                .toString();
    }

    private String createImageUrl(String photoReference) {
        return new StringBuilder(PHOTO_API_URL)
                .append("?photoreference=").append(photoReference)
                .append("&key=").append(apiKey)
                .toString();
    }

    private String getDistrictName(Long placeId) {
        District district = districtRepository.findById(placeId).orElseThrow(() -> new CustomException(REGION_NOT_FOUND));
        return new StringBuilder(district.getCountry().getCity().getName()).append(" ")
                .append(district.getCountry().getName()).append(" ")
                .append(district.getName())
                .toString();
    }
}