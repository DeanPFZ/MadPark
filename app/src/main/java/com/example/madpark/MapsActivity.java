package com.example.madpark;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.madpark.tools.GarageAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final Map<String, Double[]> parkingLocations = new LinkedHashMap<String, Double[]>() {
        {
            put("Brayton Parking Lot", new Double[]{43.076726, -89.380210});
            put("Capitol Square North Garage", new Double[]{43.077692, -89.383034});
            put("Government East Garage", new Double[]{43.073948, -89.379849});
            put("Overture Center Garage", new Double[]{43.073551, -89.389399});
            put("South Livingston Street Garage", new Double[]{43.080034, -89.373396});
            put("State Street Campus Garage", new Double[]{43.074132, -89.397212});
            put("State Street Capitol Parking Ramp", new Double[]{43.075576, -89.387533});
            put("University Avenue Ramp", new Double[]{44.976050, -93.228652});
            put("Nancy Nicholas Hall Garage", new Double[]{43.075790, -89.409479});
            put("Observatory Drive Ramp", new Double[]{43.076211, -89.414062});
            put("Helen C. White Garage (Lower)", new Double[]{43.076906, -89.400809});
            put("Helen C. White Garage (Upper)", new Double[]{43.076906, -89.400809});
            put("Grainger Hall Garage", new Double[]{43.073130, -89.402094});
            put("N Park Street Ramp", new Double[]{43.068436, -89.399883});
            put("Lake & Johnson Ramp", new Double[]{43.072570, -89.396657});
            put("Fluno Center Garage", new Double[]{43.073320, -89.396448});
            put("Engineering Drive Ramp (lot 17)", new Double[]{43.072034, -89.412244});
            put("Union South Garage Lot 80", new Double[]{43.071432, -89.408490});
            put("University Bay Drive Ramp", new Double[]{43.081431, -89.428221});
        }
    };
    final static String TAG = "MAP";
    Map<String, Integer> parkingLocationPos = new HashMap<String, Integer>();

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    boolean firstLaunch = true;
    GarageAvailability myGarageAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setTitle("MadPark");

        myGarageAvailability = new GarageAvailability();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        markParkingLocations(mGoogleMap);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // 5 seconds
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void markParkingLocations(GoogleMap mGoogleMap) {
        int i = 0;
        for (Map.Entry<String, Double[]> entry : parkingLocations.entrySet()) {
            Double[] latLonSet = entry.getValue();
            LatLng latLng = new LatLng(latLonSet[0], latLonSet[1]);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(entry.getKey())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    .setTag(0);
            if (firstLaunch) {
                parkingLocationPos.put(entry.getKey(), i);
            }
            i++;
            // Set a listener for marker click.
            mGoogleMap.setOnMarkerClickListener(this);
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Get the current lat and lon
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //move map camera
                if (firstLaunch) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                firstLaunch = false;
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        final ArrayList<String> myParkingSpots = myGarageAvailability.getRampAndSpots();
        final ArrayList<Uri> myUWMap = myGarageAvailability.getMapOfUW();
        System.out.println("The parking spots: " + myParkingSpots);
        System.out.println("The myUWMap: " + myUWMap);
        System.out.println("The parkingLocationPos: " + parkingLocationPos);
        Toast.makeText(this,
                marker.getTitle() +
                        " has been clicked",
                Toast.LENGTH_SHORT).show();
        int pos = parkingLocationPos.get(marker.getTitle());
        if (myParkingSpots != null && myUWMap != null && myParkingSpots.size() != 0 && myUWMap.size() != 0) {
            Intent intent = new Intent(MapsActivity.this, DialogActivity.class);
            intent.putExtra("name", marker.getTitle());
            intent.putExtra("availability", myParkingSpots.get(pos));
            intent.putExtra("website", myUWMap.get(pos).toString());
            startActivity(intent);
        }
        return false;
    }

    public void startFindingIntent(View view) {
        Intent intent = new Intent(MapsActivity.this, DestinationActivity.class);
        startActivity(intent);
    }
}