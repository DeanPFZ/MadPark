package com.example.madpark.tools;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

public class RecommendationGenerator {
    private Location destLocation;
    private Map<String, Double[]> parkingLocationMap;
    private String parkingLocationKey;
    private Uri parkingLocationWebsite;

    public RecommendationGenerator(Location destLocation, Map<String, Double[]> parkingLocationMap) {
        this.destLocation = destLocation;
        this.parkingLocationMap = parkingLocationMap;
    }

    public void generateClosest(ArrayList<String> myParkingSpots, ArrayList<Uri> myUWMap) {
        double destLatitude = destLocation.getLatitude();
        double destLongtitude = destLocation.getLongitude();
        double minDistance = Double.MAX_VALUE;
        String minDistanceLotName = "";
        int pos = 0;
        for (Map.Entry<String, Double[]> entry : parkingLocationMap.entrySet()) {
            Double[] latLonSet = entry.getValue();
            double parkingSpotLat = latLonSet[0];
            double parkingSpotLong = latLonSet[1];
            double distanceSqr = Math.pow(parkingSpotLat - destLatitude, 2) +
                    Math.pow(parkingSpotLong - destLongtitude, 2);
            Log.i("tag", "Recommendation:  distanceSqr:" + distanceSqr + " and the name is: "
                    + entry.getKey() + " with availability: " + Integer.valueOf(myParkingSpots.get(pos).replaceAll("[^0-9]", "")));
            if (minDistance >= distanceSqr && Integer.valueOf(myParkingSpots.get(pos).replaceAll("[^0-9]", "")) > 0) {
                minDistance = distanceSqr;
                minDistanceLotName = entry.getKey();
                parkingLocationWebsite = myUWMap.get(pos);
                Log.i("tag", "Recommendation: Lot name" + minDistanceLotName + " with availability" +
                        " of " + Integer.valueOf(myParkingSpots.get(pos).replaceAll("[^0-9]", "")));
            }
            pos++;
        }
        if(minDistance >= .00013 ) {
            parkingLocationKey = "It looks like your location is outside downtown/campus. Either street park nearby or your destination may have parking on site.";
            parkingLocationWebsite = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + destLatitude + "," + destLongtitude);
        }
        else {
            parkingLocationKey = minDistanceLotName;
        }

    }

    public Uri getParkingLocationWebsite(){
        return parkingLocationWebsite;
    }

    public String getParkingLocationKey() {
        return parkingLocationKey;
    }
}
