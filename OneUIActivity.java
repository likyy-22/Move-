package com.example.motion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OneUIActivity extends AppCompatActivity {

    private static final double RADIUS = 10000; // 10 km radius in meters
    private static final String API_KEY = "AIzaSyBuwQs-lXDhOMZ1bJdkI95kSaG5pUgxcYM"; // Replace with your API key
    private List<Hotel> hotelList;
    private RecyclerView recyclerView;
    private CardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oneui);

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the hotel list
        hotelList = new ArrayList<>();
        adapter = new CardAdapter(hotelList);
        recyclerView.setAdapter(adapter);

        // Fetch the latitude and longitude from intent extras
        double latitude = getIntent().getDoubleExtra("latitude", 37.7749); // Default to San Francisco
        double longitude = getIntent().getDoubleExtra("longitude", -122.4194); // Default to San Francisco
        LatLng currentLocation = new LatLng(latitude, longitude);

        // Fetch nearby hotels based on the received location
        fetchNearbyHotels(currentLocation);

        // Initialize the profile button click listener
        Button userProfileButton = findViewById(R.id.user_profile_button);
        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(OneUIActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void fetchNearbyHotels(LatLng location) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                location.latitude + "," + location.longitude +
                "&radius=" + RADIUS +
                "&type=lodging&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject hotel = results.getJSONObject(i);
                                String name = hotel.getString("name");
                                String description = hotel.optString("vicinity", "No description available");
                                String imageUrl = null;

                                // Check if the hotel has photos
                                if (hotel.has("photos")) {
                                    JSONArray photos = hotel.getJSONArray("photos");
                                    String photoReference = photos.getJSONObject(0).getString("photo_reference");
                                    imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                                            photoReference + "&key=" + API_KEY;
                                }

                                // Add the hotel to the list
                                hotelList.add(new Hotel(name, description, imageUrl));
                            }

                            // Notify the adapter of data changes
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error fetching hotels: " + error.getMessage());
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
