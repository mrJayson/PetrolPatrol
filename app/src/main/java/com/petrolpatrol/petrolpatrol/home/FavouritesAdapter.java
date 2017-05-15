package com.petrolpatrol.petrolpatrol.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.util.Colour;
import com.petrolpatrol.petrolpatrol.util.Gradient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        View colour;
        TextView price;
        TextView name;
        TextView address;
        ViewHolder(View itemView) {
            super(itemView);

            colour = itemView.findViewById(R.id.item_favourites_colour);
            price = (TextView) itemView.findViewById(R.id.item_favourites_price);
            name = (TextView) itemView.findViewById(R.id.item_favourites_name);
            address = (TextView) itemView.findViewById(R.id.item_favourites_address);
        }
    }

    private Context context;
    private Map<String, Average> averages;
    private Gradient gradient;
    private List<Integer> favourites;
    private View emptyView;

    FavouritesAdapter(Map<String, Average> averages, Context context) {
        this.context = context;
        this.averages = averages;
        this.gradient = new Gradient(context);
        this.favourites = new ArrayList<>();
        this.favourites = Preferences.getInstance(context).getFavourites();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favourites, parent, false);
        return new FavouritesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Station station;
        SQLiteClient client = new SQLiteClient(context);
        client.open();
        station = client.getStation(favourites.get(position));
        client.close();

        FuelCheckClient fuelClient = new FuelCheckClient(context);
        fuelClient.getFuelPricesForStation(station.getId(), new FuelCheckClient.FuelCheckResponse<List<Price>>() {
            @Override
            public void onCompletion(List<Price> res) {
                for (Price p : res) {
                    station.setPrice(p);
                }
                String selectedFuelType = Preferences.getInstance().getString(Preferences.Key.SELECTED_FUELTYPE);
                if (averages.get(selectedFuelType) != null) {
                    gradient.setMeanPrice(averages.get(selectedFuelType).getPrice());
                    Colour c = gradient.gradiateColour(station.getPrice(selectedFuelType).getPrice());
                    holder.colour.setBackgroundColor(c.integer);

                    holder.price.setText(String.valueOf(station.getPrice(selectedFuelType).getPrice()));
                    holder.name.setText(station.getName());
                    holder.address.setText(station.getAddress());
                }
            }
        });
    }

    public void refresh() {
        List<Integer> refreshedList = Preferences.getInstance(context).getFavourites();
        if (favourites.size() != refreshedList.size() || !favourites.containsAll(refreshedList) || !refreshedList.containsAll(favourites)) {
            favourites = refreshedList;
            notifyDataSetChanged();
            updateEmptyView();
        }
        notifyDataSetChanged();
    }

    public void setEmptyView(View view) {
        emptyView = view;
    }

    private void updateEmptyView() {
        if (emptyView != null && favourites != null) {
            boolean showEmptyView = getItemCount() == 0;
            emptyView.setVisibility(showEmptyView ? VISIBLE : GONE);

        }
    }

    @Override
    public int getItemCount() {
        return favourites.size();
    }
}
