package com.bungaebowling.server.place.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<?> getPlaceDetails(@RequestParam Double latitude,
                                  @RequestParam Double longitude) {
        String response = placeService.getPlaceDetails(latitude, longitude);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }
}
