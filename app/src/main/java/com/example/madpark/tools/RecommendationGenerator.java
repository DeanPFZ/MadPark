package com.example.madpark.tools;

import android.location.Location;

import java.util.Map;

public class RecommendationGenerator {
    private Location destLocation;
    private Map<String, Double[]> parkingLocationMap;
    private String parkingLocationKey;

    public RecommendationGenerator(Location destLocation, Map<String, Double[]> parkingLocationMap) {
        this.destLocation = destLocation;
        this.parkingLocationMap = parkingLocationMap;
        generateClosest();
    }

    private void generateClosest() {
        double destLatitude = destLocation.getLatitude();
        double destLongtitude = destLocation.getLongitude();
        double minDistance = 0;
        String minDistanceLotName = "";
        for (Map.Entry<String, Double[]> entry : parkingLocationMap.entrySet()) {
            Double[] latLonSet = entry.getValue();
            double parkingSpotLat = latLonSet[0];
            double parkingSpotLong = latLonSet[1];
            double distanceSqr = Math.pow(parkingSpotLat - destLatitude, 2) +
                    Math.pow(parkingSpotLong - destLongtitude, 2);
            if (minDistance >= distanceSqr) {
                minDistance = distanceSqr;
                minDistanceLotName = entry.getKey();
            }
        }
        parkingLocationKey = minDistanceLotName;
    }

    public String getParkingLocationKey() {
        return parkingLocationKey;
    }
}
