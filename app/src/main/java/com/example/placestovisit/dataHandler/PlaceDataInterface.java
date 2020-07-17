package com.example.placestovisit.dataHandler;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface PlaceDataInterface {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void addPlace(Place place);
    @Update
    void updatePlace(Place place);
    @Delete
    void deletePlace(Place place);
    @Transaction
    @Query("SELECT * FROM place_data ORDER BY placeVisited")
    List<Place> getAllPlaces();

}
