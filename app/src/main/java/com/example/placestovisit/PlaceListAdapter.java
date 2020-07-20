package com.example.placestovisit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.placestovisit.dataHandler.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceListHolder> implements Filterable {

    private Context context;
    private List<Place> placeList = new ArrayList<>();

    public PlaceListAdapter(List<Place> placeList, Context context) {
        this.placeList = placeList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.place_list_cell, parent, false);
        return new PlaceListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceListHolder holder, final int position) {
        holder.placeName.setText(placeList.get(position).getPlaceName());
        if(placeList.get(position).isPlaceVisited()) {
            holder.cellLayout.setBackgroundColor(Color.rgb(188, 245, 197));
        }
        else {
            holder.cellLayout.setBackgroundColor(Color.rgb(245, 197, 66));
        }

        holder.cellLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("saved", placeList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public Place getPlaceAtPosition(int position) {
        return placeList.get(position);
    }

    public void updateData(List<Place> placeList) {
        this.placeList = placeList;
    }

    @Override
    public Filter getFilter() {
        return placeFilter;
    }

    private Filter placeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Place> filteredPlace = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0) {
                filteredPlace.addAll(placeList);
            }
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(Place place: placeList) {
                    if(place.getPlaceName().toLowerCase().trim().contains(filterPattern) ||
                            place.getPlaceDetails().toLowerCase().trim().contains(filterPattern)) {
                        filteredPlace.add(place);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredPlace;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            placeList.clear();
            placeList.addAll((List<Place>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class PlaceListHolder extends RecyclerView.ViewHolder {

        TextView placeName;
        ConstraintLayout cellLayout;

        public PlaceListHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            cellLayout = itemView.findViewById(R.id.place_list_cell);
        }
    }
}
