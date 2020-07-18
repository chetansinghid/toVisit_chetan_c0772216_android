package com.example.placestovisit.dataHandler;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "place_data")
public class Place implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String placeName;
    @NonNull
    private String placeDetails;
    @NonNull
    private Date placeSavedDate;
    @NonNull
    private double placeLat;
    @NonNull
    private double placeLong;
    @NonNull
    private boolean placeVisited;

//    constructor
    public Place(@NonNull String placeName, @NonNull String placeDetails, double placeLat, double placeLong) {
        this.placeName = placeName;
        this.placeDetails = placeDetails;
        this.placeLat = placeLat;
        this.placeLong = placeLong;
        this.placeVisited = false;
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        this.placeSavedDate = date;
    }

//    getters and setters


    public String getPlaceDetails() {
        return placeDetails;
    }

    public void setPlaceDetails(String placeDetails) {
        this.placeDetails = placeDetails;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(@NonNull String placeName) {
        this.placeName = placeName;
    }

    @NonNull
    public Date getPlaceSavedDate() {
        return placeSavedDate;
    }

    public void setPlaceSavedDate(@NonNull Date placeSavedDate) {
        this.placeSavedDate = placeSavedDate;
    }

    public double getPlaceLat() {
        return placeLat;
    }

    public void setPlaceLat(double placeLat) {
        this.placeLat = placeLat;
    }

    public double getPlaceLong() {
        return placeLong;
    }

    public void setPlaceLong(double placeLong) {
        this.placeLong = placeLong;
    }

    public boolean isPlaceVisited() {
        return placeVisited;
    }

    public void setPlaceVisited(boolean placeVisited) {
        this.placeVisited = placeVisited;
    }
}
