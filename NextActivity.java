package com.example.motion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NextActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private Marker currentMarker; // Variable to hold the marker
    private static final String API_KEY = "AIzaSyBuwQs-lXDhOMZ1bJdkI95kSaG5pUgxcYM"; // Replace with your actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next); // Your layout file

        // Initialize the fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the "Confirm Location" button
        Button confirmButton = findViewById(R.id.confirm_location_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    // Pass the latitude and longitude to the next activity
                    Intent intent = new Intent(NextActivity.this, OneUIActivity.class);
                    intent.putExtra("latitude", currentLocation.latitude);
                    intent.putExtra("longitude", currentLocation.longitude);
                    startActivity(intent);
                } else {
                    Toast.makeText(NextActivity.this, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Get the current location and move the camera
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        // Add a marker at the current location and make it draggable
                        currentMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("You are here")
                                .draggable(true));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        Toast.makeText(this, "Failed to get current location", Toast.LENGTH_LONG).show();
                    }
                });

        // Set a map click listener to place the marker where the user taps
        mMap.setOnMapClickListener(latLng -> {
            if (currentMarker != null) {
                currentMarker.setPosition(latLng);
            } else {
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            }
            currentLocation = latLng; // Update currentLocation to the new position
        });

        // Set a listener for marker drag events
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // Optional: You can handle actions when dragging starts
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Optional: Handle actions while dragging
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentLocation = marker.getPosition(); // Update currentLocation when drag ends
                Toast.makeText(NextActivity.this, "Marker moved to: " + currentLocation.latitude + ", " + currentLocation.longitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                onMapReady(mMap);
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
