package com.example.placestovisit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.placestovisit.dataHandler.Place;
import com.example.placestovisit.dataHandler.PlacesHelperRepository;

public class PlaceListActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;
    private PlacesHelperRepository placesHelperRepository;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);
        checkRequiredPermissions();
        setupInitialViews();
    }

    private void setupInitialViews() {
        recyclerView = findViewById(R.id.recycler_view);
        placesHelperRepository = new PlacesHelperRepository(this.getApplication());
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
        placesHelperRepository.getAllPlaces();

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
}