package com.bungaebowling.server.place.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.place.dto.PlaceResponse;
import com.bungaebowling.server.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/{placeId}")
    public ResponseEntity<?> getPlaceDetails(@PathVariable Long placeId, @RequestParam String name) {
        PlaceResponse.GetPlaceDto response = placeService.getPlaceDetails(name, placeId);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }
}
