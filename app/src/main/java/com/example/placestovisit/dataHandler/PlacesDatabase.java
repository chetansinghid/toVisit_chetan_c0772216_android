package com.example.placestovisit.dataHandler;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Place.class}, version = 1, exportSchema = false)
@TypeConverters({ConvertDatatype.class})
public abstract class PlacesDatabase extends RoomDatabase  {

    private static final String PLACES_DB = "placesDatabase.db";
    public static volatile PlacesDatabase instance;
    public abstract PlaceDataInterface placeDataInterface();

    static synchronized PlacesDatabase getInstance(Context context) {
        if(instance == null) {
            instance = createInstance(context);
        }
        return instance;
    }

    private static PlacesDatabase createInstance(final Context context) {
        return Room.databaseBuilder(context, PlacesDatabase.class, PLACES_DB)
                .fallbackToDestructiveMigration()
                .build();
    }
}
