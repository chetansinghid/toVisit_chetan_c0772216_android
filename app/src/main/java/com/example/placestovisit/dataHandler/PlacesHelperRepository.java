package com.example.placestovisit.dataHandler;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlacesHelperRepository {

    private static PlaceDataInterface placeDataInterface;

    public PlacesHelperRepository(Application application) {
        PlacesDatabase placesDatabase = PlacesDatabase.getInstance(application);
        placeDataInterface = placesDatabase.placeDataInterface();
    }

    public void insertPlaceToDatabase(Place place) {
        if(place == null) {
            Log.i("DB insertion error!", "Null value provided");
        }
        else {
            new PlacesHelperRepository.InsertPlace(placeDataInterface).execute(place);
        }
    }

    public void updatePlaceInDatabase(Place place) {
        if(place == null) {
            Log.i("DB insertion error!", "Null value provided");
        }
        else {
            new PlacesHelperRepository.UpdatePlace(placeDataInterface).execute(place);
        }
    }

    public void deletePlaceFromDatabase(Place place) {
        if(place == null) {
            Log.i("DB insertion error!", "Null value provided");
        }
        else {
            new PlacesHelperRepository.DeletePlace(placeDataInterface).execute(place);
        }
    }

    public List<Place> getAllPlaces() {
        List<Place> placeList = new ArrayList<>();
        try {
            placeList = new FetchAllPlaces(placeDataInterface).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return placeList;
    }

//    async classes implemented

    private static class InsertPlace extends AsyncTask<Place, Void, Void> {

        private PlaceDataInterface placeDataInterface;

        private InsertPlace(PlaceDataInterface placeDataInterface) {
            this.placeDataInterface = placeDataInterface;
        }

        @Override
        protected Void doInBackground(Place... places) {
            placeDataInterface.addPlace(places[0]);
            return null;
        }
    }

    private static class UpdatePlace extends AsyncTask<Place, Void, Void> {

        private PlaceDataInterface placeDataInterface;

        private UpdatePlace(PlaceDataInterface placeDataInterface) {
            this.placeDataInterface = placeDataInterface;
        }


        @Override
        protected Void doInBackground(Place... places) {
            placeDataInterface.updatePlace(places[0]);
            return null;
        }
    }

    private static class DeletePlace extends AsyncTask<Place, Void, Void> {

        private PlaceDataInterface placeDataInterface;

        private DeletePlace(PlaceDataInterface placeDataInterface) {
            this.placeDataInterface = placeDataInterface;
        }


        @Override
        protected Void doInBackground(Place... places) {
            placeDataInterface.deletePlace(places[0]);
            return null;
        }
    }

    private static class FetchAllPlaces extends AsyncTask<Void, Void, List<Place>> {

        private PlaceDataInterface placeDataInterface;

        private FetchAllPlaces(PlaceDataInterface placeDataInterface) {
            this.placeDataInterface = placeDataInterface;
        }


        @Override
        protected List<Place> doInBackground(Void... voids) {
            return placeDataInterface.getAllPlaces();
        }
    }
}
