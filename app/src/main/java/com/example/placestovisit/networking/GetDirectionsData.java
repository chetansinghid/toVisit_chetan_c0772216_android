package com.example.placestovisit.networking;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.HashMap;

public class GetDirectionsData extends AsyncTask<Object, Void, String> {

    String googleDirectionData;
    GoogleMap googleMap;
    String url;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        googleMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];

        GetUrl fetchURL = new GetUrl();
        try {
            googleDirectionData = fetchURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String> distances = null;
        DataParser directionParser = new DataParser();
        distances = directionParser.parseDistance(s);

        String distance = distances.get("distance");
        String duration = distances.get("duration");

        String[] directionsList;
        directionsList = directionParser.parseDirections(s);
        displayDirection(directionsList, distance, duration);
    }

    private void displayDirection(String[] directionsList, String distance, String duration) {
        googleMap.clear();
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title("Duration : " + duration)
                .snippet("Distance : " + distance)
                .draggable(true);
        googleMap.addMarker(options);
        for (int i=0; i<directionsList.length; i++) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .addAll(PolyUtil.decode(directionsList[i]));
            googleMap.addPolyline(polylineOptions);
        }
    }
}
