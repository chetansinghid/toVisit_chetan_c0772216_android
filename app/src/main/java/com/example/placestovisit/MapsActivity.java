package com.example.placestovisit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.placestovisit.dataHandler.Place;
import com.example.placestovisit.dataHandler.PlacesHelperRepository;
import com.example.placestovisit.networking.GetDirectionsData;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceDataInterface {

    private GoogleMap mMap;
    private LatLng myLocation;
    private LatLng myPlacePosition;
    private LatLng getMyPlacePositionTemp;
    private Marker myPlaceMarker;
    private ArrayList<String> myPlaceName;
    private Place place;
    private TextView showDistanceView;
    private TextView showDistanceDetailsView;
    private boolean isNew = true;
    private PlacesHelperRepository placesHelperRepository;
    private Polyline currentPolyline;

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


        setupDatabaseMethods();
        setupButtonsAndTextViews();
        setupSpinnerHandler();
    }

//map view and button handlers
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdate();
        setupInitialMapView();
        setUpMapGestures();
        checkIfSavedLocationOpened();
    }

//    initial load methods

    private void setupInitialMapView() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this,"Location permission Required for app usage! Please grant permission by going to settings.", Toast.LENGTH_LONG);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

    }

    private void setUpMapGestures() {
        setupAddPlaceGesture();
        setUpMarkerDragGesture();
        setupMarkerClickGesture();
    }

    private void setupAddPlaceGesture() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(place == null) {
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
                addMarkerToMap(marker.getPosition());
                updatePlaceToDb();
            }
        });
    }

    private void updatePlaceToDb() {
        //                updates object
        place.setPlaceLat(myPlacePosition.latitude);
        place.setPlaceLong(myPlacePosition.longitude);
        place.setPlaceName(myPlaceName.get(0));
        place.setPlaceDetails(myPlaceName.get(1));
//                saves object
        placesHelperRepository.updatePlaceInDatabase(place);
    }

    private void setupMarkerClickGesture() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getMyPlacePositionTemp = (LatLng) marker.getTag();
                return false;
            }
        });
    }

    private void checkIfSavedLocationOpened() {
        if(getIntent().hasExtra("saved")) {
            Log.i("maps activity","reaching here!");
            isNew = false;
            place = (Place) getIntent().getSerializableExtra("saved");
            addMarkerToMap(new LatLng(place.getPlaceLat(), place.getPlaceLong()));
        }
    }

    private void addMarkerToMap(LatLng latLng) {
        mMap.clear();
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
        if(myPlacePosition != null && myLocation != null) {
            String url = getDirectionUrl();
            Object[] dataTransfer = new Object[3];

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            dataTransfer[2] = myPlacePosition;

            GetDirectionsData getDirectionsData = new GetDirectionsData();
            // execute asynchronously
            System.out.print(url);
            getDirectionsData.execute(dataTransfer);
        }
    }


    public void setAsFavorite(View view) {
        if(getMyPlacePositionTemp == null) {
            if(myPlacePosition != null) {
                if(isNew) {
                    addPlaceToDatabase();
                    isNew = false;
                    Toast.makeText(MapsActivity.this, "Place Added As Favorite!", Toast.LENGTH_LONG).show();
                }
                else {
                    removeMarkerAndDeletePlace();
                    isNew = true;
                    Toast.makeText(MapsActivity.this, "Place Removed From Favorite!", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            addMarkerToMap(getMyPlacePositionTemp);
            if(place == null) {

                addPlaceToDatabase();
                isNew = false;
                Toast.makeText(MapsActivity.this, "Place Added As Favorite!", Toast.LENGTH_LONG).show();
            }
            else {
                updatePlaceToDb();
                Toast.makeText(MapsActivity.this, "Favorite Place Updated!", Toast.LENGTH_LONG).show();
            }
            getMyPlacePositionTemp = null;
        }

    }

    private void addPlaceToDatabase() {
        place = new Place(myPlaceName.get(0), myPlaceName.get(1), myPlacePosition.latitude, myPlacePosition.longitude);
        placesHelperRepository.insertPlaceToDatabase(place);
    }

    private void removeMarkerAndDeletePlace() {
        placesHelperRepository.deletePlaceFromDatabase(place);
        mMap.clear();
        myPlaceMarker = null;
        myPlacePosition = null;
        myPlaceName = null;
        place = null;
        showDistanceDetailsView.setText("");

    }

    private void setDistanceTextView() {
        if(myLocation != null && myPlaceMarker != null) {
            float[] results = new float[10];
            Location.distanceBetween(myLocation.latitude, myLocation.longitude, myPlacePosition.latitude, myPlacePosition.longitude, results);
            String yourLocation = "" + myPlaceName.get(0);
            showDistanceView.setText((String) yourLocation);
            String details = "Distance: " + Math.round(results[0]/100)/10.0 + " KM";
            showDistanceDetailsView.setText((String)details);
        }
        if(myLocation != null && myPlaceMarker == null) {
            String text = getLocationAddressDetails(myLocation).get(0);
            showDistanceView.setText(text);
        }
    }


//    helper methods

    //  fetches the geocoder details of latlangs
    private ArrayList<String> getLocationAddressDetails(LatLng location) {
        ArrayList<String> addressDetails = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            Address address = addressList.get(0);
            String title = address.getSubThoroughfare() + ", " + address.getThoroughfare();
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

    private String getPlaceUrl(String placeType) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+myLocation.latitude+","+myLocation.longitude);
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
                setupCameraZoom();
                Log.i("im method!", "Location getting set");
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    private void setupCameraZoom() {
        if(isNew) {
            if(myLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
            }
        }
        else {
            if(myLocation != null) {
                LatLngBounds.Builder b = new LatLngBounds.Builder();
                b.include(myLocation);
                b.include(myPlacePosition);

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 300 ));
            }
        }
    }

    @Override
    public void destinationSelected() {

    }

    // button handlers
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.maps_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_place);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                showNearbyPlaces(getPlaceUrl(s));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mMap.clear();
                if(myPlaceMarker != null) {
                    addMarkerToMap(myPlacePosition);
                }
                return false;
            }
        });
        return true;
    }

    private void setupButtonsAndTextViews() {
        showDistanceView = findViewById(R.id.distance_text_view);
        showDistanceDetailsView = findViewById(R.id.distance_text_view_details);
    }

    private void setupDatabaseMethods() {
        placesHelperRepository = new PlacesHelperRepository(MapsActivity.this.getApplication());
    }

    private void setupSpinnerHandler() {
        Spinner spinner = findViewById(R.id.select_maps);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.map_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:  mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1: mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2: mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 3: mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.show_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Welcome to My Place List!");
            builder.setMessage("This is an app where you can save the places you want to visit." +
                    "To save a place simply click on + button and you will be taken to a map scree." +
                    "You can then simply long tap on map to add a place. Pressing on the star button will save the location!");

            builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("Navigating the list view!");
                    builder.setMessage("Once you have marked a place as favorite, you can see that on your home page." +
                            "You can swipe right to mark it as visited, and the cell will highlight as Green as confirmation." +
                            "You can also delete the place by swiping left.");
                    builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                            builder.setTitle("Navigating the map view!");
                            builder.setMessage("You can open a place by tapping on its cell. Once opened, you can drag marker to" +
                                    "change the place. You can press on star button again to delete the place. You can get directions" +
                                    " to the place by pressing the directions button, and you can search for place on search bar");

                            builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                    builder.setTitle("Searching for places!");
                                    builder.setMessage("You can add the place as favorite by tapping on the search results icon, and that" +
                                            " will save it as your favorite, or replace it if you opened previously saved marker. You can" +
                                            " see the distance and duration from the place on your app.");

                                    builder.setNegativeButton("Start using the app!", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
                            builder.setNegativeButton("Start using the app!", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                    builder.setNegativeButton("Start using the app!", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            builder.setNegativeButton("Start using the app!", null);
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}