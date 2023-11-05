package com.bungaebowling.server.place.dto;

import java.util.List;

public class PlaceResponse {

    public record GetPlaceDto(
            String placeName,
            List<String> images,
            String address,
            String phoneNumber,
            List<String> operationTime
    ){}
}
