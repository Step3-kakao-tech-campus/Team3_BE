package com.bungaebowling.server.place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PlaceService {

    @Value("${google.api.places.key}")
    private String apiKey;

    private final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/details/json";

    @Transactional
    public String getPlaceDetails(Double latitude, Double longitude){
        String placeId = getGooglePlaceId(latitude, longitude);
        log.info("key=" + apiKey);
        String url = String.format("%s?place_id=%s&key=%s", PLACES_API_URL, placeId, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    private String getGooglePlaceId(Double latitude, Double longitude){
        String url = String.format("%s?latlng=%f,%f&key=%s", GEOCODING_API_URL, latitude, longitude, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        return extractPlaceId(response);
    }

    private String extract

    private String extractPlaceId(String response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultsNode = rootNode.path("results");
            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstResult = resultsNode.get(0);
                return firstResult.path("place_id").asText();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); //TODO: 수정
        }

        return null;
    }
}
