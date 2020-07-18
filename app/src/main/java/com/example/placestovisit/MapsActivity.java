package com.example.placestovisit;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.placestovisit.dataHandler.Place;
import com.example.placestovisit.dataHandler.PlacesHelperRepository;
import com.example.placestovisit.networking.GetNearbyPlaces;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng myLocation;
    private LatLng myPlacePosition;
    private Marker myPlaceMarker;
    private ArrayList<String> myPlaceName;
    private Place place;
    private Button setAsMyPlaceButton;
    private TextView showDistanceView;
    private TextView showDistanceDetailsView;
    private boolean isNew = true;
    private PlacesHelperRepository placesHelperRepository;

//    constant strings
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 3000;
    private static final int RADIUS = 1500;

    // use the fused location provider client
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocationUpdate();
        setupDatabaseMethods();
        setupButtonsAndTextViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupInitialMapView();
        setupAddPlaceGesture();
        setUpMarkerDragGesture();
        checkIfSavedLocationOpened();
        setupSpinnerHandler();
    }

//    initial load methods

    private void setupButtonsAndTextViews() {
        showDistanceView = findViewById(R.id.distance_text_view);
        showDistanceDetailsView = findViewById(R.id.distance_text_view_details);
    }

    private void setupInitialMapView() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this,"Location permission Required for app usage! Please grant permission by going to settings.", Toast.LENGTH_LONG);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void setupAddPlaceGesture() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(myPlaceMarker == null) {
                    addMarkerToMap(latLng);
                }
            }
        });
    }

    private void setUpMarkerDragGesture() {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                myPlacePosition = marker.getPosition();
                myPlaceName = getLocationAddressDetails(myPlacePosition);
//                updates marker
                marker.setTitle(myPlaceName.get(0));
                marker.setSnippet(myPlaceName.get(1));
//                updates object
                place.setPlaceLat(myPlacePosition.latitude);
                place.setPlaceLong(myPlacePosition.longitude);
                place.setPlaceName(myPlaceName.get(0));
                place.setPlaceDetails(myPlaceName.get(1));
//                saves object
                placesHelperRepository.updatePlaceInDatabase(place);
//                updates upper textview
                setDistanceTextView();
            }
        });
    }

    private void setupSpinnerHandler() {
        Spinner spinner = findViewById(R.id.select_places);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(MapsActivity.this, R.array.spinner_selection, android.R.layout.simple_spinner_dropdown_item);

    }

    private void setupDatabaseMethods() {
        placesHelperRepository = new PlacesHelperRepository(MapsActivity.this.getApplication());
    }

    private void checkIfSavedLocationOpened() {
        if(getIntent().hasExtra("saved")) {
            isNew = false;
            place = (Place) getIntent().getSerializableExtra("saved");
            addMarkerToMap(new LatLng(place.getPlaceLat(), place.getPlaceLong()));
        }
    }

    private void addMarkerToMap(LatLng latLng) {
        myPlacePosition = latLng;
        myPlaceName = getLocationAddressDetails(myPlacePosition);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(myPlacePosition);
        markerOptions.draggable(true);
        markerOptions.title(myPlaceName.get(0));
        markerOptions.snippet(myPlaceName.get(1));
        myPlaceMarker = mMap.addMarker(markerOptions);
        setDistanceTextView();
    }

    public void showDirections(View view) {
        if(myPlacePosition != null) {

        }
    }

    public void setAsFavorite(View view) {
        if(myPlacePosition != null) {
            if(isNew) {
//                setAsMyPlaceButton.setBackgroundColor(Color.RED);
                addPlaceToDatabase();
                isNew = false;
            }
            else {
//                setAsMyPlaceButton.setBackgroundColor(Color.YELLOW);
                removeMarkerAndDeletePlace();
                isNew = true;
            }
        }
    }

    private void addPlaceToDatabase() {
        place = new Place(myPlaceName.get(0), myPlaceName.get(1), myPlacePosition.latitude, myPlacePosition.longitude);
        placesHelperRepository.insertPlaceToDatabase(place);
    }

    private void removeMarkerAndDeletePlace() {
        placesHelperRepository.deletePlaceFromDatabase(place);
        myPlaceMarker.remove();
        myPlaceMarker = null;
        myPlacePosition = null;
        myPlaceName = null;
        place = null;
        resetDistanceTextView();
    }

    private void setDistanceTextView() {
        if(myPlaceMarker == null) {
            resetDistanceTextView();
        }
        else {
            showDistanceView.setText((String)myPlaceName.get(0));
            showDistanceDetailsView.setText((String)"Distance");
//            showDistanceView.setText(myPlaceName.get(0));
//            String distance = "Distance: " + "getdistance";
//            showDistanceDetailsView.setText(distance);
        }
    }

    private void resetDistanceTextView() {
        showDistanceView.setText("My Location");
        showDistanceDetailsView.setText("Tap or search to add Location");
    }


//    helper methods

    //  fetches the geocoder details of latlangs
    private ArrayList<String> getLocationAddressDetails(LatLng location) {
        ArrayList<String> addressDetails = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            Address address = addressList.get(0);
            String title = address.getThoroughfare() + ", " + address.getSubThoroughfare();
            String snippet = address.getLocality() + ", " + address.getAdminArea();
            addressDetails.add(title);
            addressDetails.add(snippet);
        } catch (Exception exception) {
            Log.i("Geolocation fetch error", exception.getMessage());
            addressDetails.add("");
            addressDetails.add("");
        }
        return addressDetails;
    }


    private void showNearbyPlaces(String url) {
        Object[] dataTransfer;
        dataTransfer = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
        getNearbyPlaces.execute(dataTransfer);
    }

    private String getPlaceUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append(("&radius="+RADIUS));
        googlePlaceUrl.append("&type="+placeType);
        googlePlaceUrl.append("&key="+getString(R.string.google_place_key));
        return googlePlaceUrl.toString();
    }

    private String getDirectionUrl() {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin="+myLocation.latitude+","+myLocation.longitude);
        googleDirectionUrl.append(("&destination="+myPlacePosition.latitude+","+myPlacePosition.longitude));
        googleDirectionUrl.append("&key="+getString(R.string.google_place_key));
        return googleDirectionUrl.toString();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());

            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }
}