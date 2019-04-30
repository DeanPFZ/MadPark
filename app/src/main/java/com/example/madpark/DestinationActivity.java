package com.example.madpark;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madpark.tools.GarageAvailability;
import com.example.madpark.tools.RecommendationGenerator;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.madpark.MapsActivity.parkingLocations;

public class DestinationActivity extends AppCompatActivity {

    private static String GOOGLE_PLACES_API_KEY;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 123;
    private TextView tvLocationName, myClosestRecc;
    private Double destLat;
    private Double destLon;
    private GarageAvailability myGarageAvailability;
    private Uri recommendParkingWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GOOGLE_PLACES_API_KEY = this.getString(R.string.google_maps_key);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_PLACES_API_KEY);
        }
        tvLocationName = findViewById(R.id.textView6);
        myClosestRecc = findViewById(R.id.textView5);

        Button btnPlaces = findViewById(R.id.btnPlaces);
        Button btnFindClosest = findViewById(R.id.btnFindParking);
        myGarageAvailability = new GarageAvailability();
        myClosestRecc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendTextViewClick();
            }
        });

        btnPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAutoCompleteIntent();

            }
        });
        btnFindClosest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findClosestRoute();

            }
        });
    }

    private void RecommendTextViewClick() {
        if (tvLocationName.getText() == "" || myClosestRecc.getText() == "") {
            Toast.makeText(this, "Empty Location or Recommendation Name", Toast.LENGTH_LONG).show();
            Log.i("tag", "Place: The destication is empty, returning nop");
            return;
        }
        if (recommendParkingWebsite == null) {
            Log.i("tag", "Place: Cannot fetch recommended parking lot uri");
            return;
        }
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, recommendParkingWebsite);
        startActivity(intent);

    }

    private void createAutoCompleteIntent() {
        tvLocationName.setText("");
        myClosestRecc.setText("");
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void findClosestRoute() {
        if (tvLocationName.getText() == "") {
            Toast.makeText(this, "Empty Location Name", Toast.LENGTH_LONG).show();
            Log.i("tag", "Place: The destication is empty, returning nop");
            return;
        }
        Location destLocation = new Location("");
        destLocation.setLatitude(destLat);
        destLocation.setLongitude(destLon);
        final ArrayList<String> myParkingSpots = myGarageAvailability.getRampAndSpots();
        final ArrayList<Uri> myUWMap = myGarageAvailability.getMapOfUW();
        RecommendationGenerator recommendationGenerator = new RecommendationGenerator(destLocation, parkingLocations);
        recommendationGenerator.generateClosest(myParkingSpots, myUWMap);
        recommendParkingWebsite = recommendationGenerator.getParkingLocationWebsite();
        String closestParkingKey = recommendationGenerator.getParkingLocationKey();
        Log.i("tag", "Place: The closest near: " + tvLocationName.getText() + " is: " + closestParkingKey + " and the website is: " + recommendParkingWebsite);
        myClosestRecc.setText(closestParkingKey);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("tag", "Place: " + place.getName() + ", " + place.getLatLng());
                tvLocationName.setText(place.getName());
                destLat = place.getLatLng().latitude;
                destLon = place.getLatLng().longitude;
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("tag", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


}