package com.example.placestovisit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.placestovisit.dataHandler.Place;
import com.example.placestovisit.dataHandler.PlacesHelperRepository;

import java.util.ArrayList;
import java.util.List;

public class PlaceListActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;
    private PlacesHelperRepository placesHelperRepository;
    private RecyclerView recyclerView;
    private List<Place> placeList = new ArrayList<>();
    private PlaceListAdapter placeListAdapter;
    private TextView placeCountView;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);
        askPermissions();
        setupInitialViewsAndData();
        setupRecyclerView();
        setupItemTouch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.maps_menu, menu);


        MenuItem searchItem = menu.findItem(R.id.search_place);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                resetResults();
                placeCountView.setVisibility(View.GONE);
                placeListAdapter.getFilter().filter(s);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetResults();
                placeCountView.setVisibility(View.VISIBLE);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.show_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlaceListActivity.this);
            builder.setTitle("Welcome to My Place List!");
            builder.setMessage("This is an app where you can save the places you want to visit." +
                    "To save a place simply click on + button and you will be taken to a map scree." +
                    "You can then simply long tap on map to add a place. Pressing on the star button will save the location!");

            builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlaceListActivity.this);
                    builder.setTitle("Navigating the list view!");
                    builder.setMessage("Once you have marked a place as favorite, you can see that on your home page." +
                            "You can swipe right to mark it as visited, and the cell will highlight as Green as confirmation." +
                            "You can also delete the place by swiping left.");
                    builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PlaceListActivity.this);
                            builder.setTitle("Navigating the map view!");
                            builder.setMessage("You can open a place by tapping on its cell. Once opened, you can drag marker to" +
                                    "change the place. You can press on star button again to delete the place. You can get directions" +
                                    " to the place by pressing the directions button, and you can search for place on search bar");

                            builder.setPositiveButton("Continue with tutorial", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PlaceListActivity.this);
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

    private void setupInitialViewsAndData() {
        recyclerView = findViewById(R.id.recycler_view);
        placesHelperRepository = new PlacesHelperRepository(this.getApplication());
        placeList = placesHelperRepository.getAllPlaces();
        placeCountView = findViewById(R.id.place_count);
        String text = placeList.size() + " Place(s)";
        placeCountView.setText(text);
    }


    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        placeListAdapter = new PlaceListAdapter(placeList, PlaceListActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(placeListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetResults();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resetResults();
    }

    private void resetResults() {
        placeList = placesHelperRepository.getAllPlaces();
        placeListAdapter.updateData(placeList);
        placeListAdapter.notifyDataSetChanged();
        String text = placeList.size() + " Place(s)";
        placeCountView.setText(text);
    }

    public void addPlace(View view) {
        Intent intent = new Intent(PlaceListActivity.this, MapsActivity.class);
        PlaceListActivity.this.startActivity(intent);
    }

    private void checkRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(PlaceListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(PlaceListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(PlaceListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(PlaceListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(PlaceListActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PlaceListActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(PlaceListActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                Toast.makeText(PlaceListActivity.this, "Permissions required to access location and save data in device!", Toast.LENGTH_LONG).show();
            } else
            {
                ActivityCompat.requestPermissions(PlaceListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);

            }
        }
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(PlaceListActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST);
    }

    private void setupItemTouch() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Place place = placeListAdapter.getPlaceAtPosition(viewHolder.getAdapterPosition());
                if(direction == ItemTouchHelper.LEFT) {
                    placesHelperRepository.deletePlaceFromDatabase(place);
                    resetResults();
                }
                else {
                    place.setPlaceVisited(true);
                    placesHelperRepository.updatePlaceInDatabase(place);
                    resetResults();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

}