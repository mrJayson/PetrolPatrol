package com.petrolpatrol.petrolpatrol.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.util.Colour;
import com.petrolpatrol.petrolpatrol.util.Gradient;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String TAG = makeLogTag(ListAdapter.class);

    static class ViewHolder extends RecyclerView.ViewHolder {
        View colour;
        TextView price;
        TextView name;
        TextView address;
        TextView distance;

        ViewHolder(View itemView) {
            super(itemView);
            colour = itemView.findViewById(R.id.item_list_colour);
            price = (TextView) itemView.findViewById(R.id.item_list_price);
            name = (TextView) itemView.findViewById(R.id.item_list_name);
            address = (TextView) itemView.findViewById(R.id.item_list_address);
            distance = (TextView) itemView.findViewById(R.id.item_list_distance);
        }
    }

    private List<Station> stations;
    private Map<String, Average> averages;
    private Context context;

    private Listener listener;

    private Gradient gradient;

    ListAdapter(Context context, List<Station> stations, Map<String, Average> averages, Listener listener) {
        this.context = context;
        this.stations = stations;
        this.averages = averages;
        this.listener = listener;
        this.gradient = new Gradient(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Station station = stations.get(position);

        gradient.setMeanPrice(averages.get(Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice());

        if (station.getPrice(Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE)) != null) {
            // leave blank if there isn't a price to be found, better than crashing
            double price = station.getPrice(Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice();
            holder.price.setText(String.valueOf(price));
            Colour c = gradient.gradiateColour(price);
            holder.colour.setBackgroundColor(c.integer);
        }
        holder.name.setText(station.getName());
        holder.address.setText(station.getAddress());
        holder.distance.setText(context.getString(R.string.item_list_distance, station.getDistance()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.displayDetails(station.getId());
                }
            }
        });
    }

    void sort(String order) {
        if (order.equals(context.getString(R.string.menu_sort_price))) {
            Collections.sort(stations, new Comparator<Station>() {
                @Override
                public int compare(Station station1, Station station2) {
                    return Double.compare(station1.getPrice(Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice(), station2.getPrice(Preferences.getInstance(context).getString(Preferences.Key.SELECTED_FUELTYPE)).getPrice());
                }
            });
            notifyDataSetChanged();
        }
        else if (order.equals(context.getString(R.string.menu_sort_distance))) {
            Collections.sort(stations, new Comparator<Station>() {
                @Override
                public int compare(Station station1, Station station2) {
                    return Double.compare(station1.getDistance(), station2.getDistance());
                }
            });
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public void updateStations(List<Station> stations) {
        this.stations = stations;

        notifyDataSetChanged();
    }

    public void clear() {
        stations.clear();
        notifyDataSetChanged();
    }

    interface Listener {
        void displayDetails(int stationID);
    }
}
