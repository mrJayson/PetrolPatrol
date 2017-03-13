package com.petrolpatrol.petrolpatrol.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.model.Station;

import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String TAG = makeLogTag(ListAdapter.class);

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView price;
        TextView name;
        TextView address;
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            price = (TextView) itemView.findViewById(R.id.item_list_price);
            name = (TextView) itemView.findViewById(R.id.item_list_name);
            address = (TextView) itemView.findViewById(R.id.item_list_address);
            distance = (TextView) itemView.findViewById(R.id.item_list_distance);
        }
    }

    private List<Station> stations;
    private String selectedFuelType;
    private Context context;

    private Listener parentListener;

    public ListAdapter(Context context, List<Station> stations, String selectedFuelType, Listener listener) {
        this.stations = stations;
        this.selectedFuelType = selectedFuelType;
        this.context = context;
        this.parentListener = listener;
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

        if (station.getPrice(selectedFuelType) != null) {
            // leave blank if there isn't a price to be found, better than crashing
            holder.price.setText(String.valueOf(station.getPrice(selectedFuelType).getPrice()));
        }
        holder.name.setText(station.getName());
        holder.address.setText(station.getAddress());
        holder.distance.setText(context.getString(R.string.item_list_distance, station.getDistance()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parentListener != null) {
                    parentListener.displayDetailsFragment(station.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public void clear() {
        stations.clear();
        notifyDataSetChanged();
    }

    public interface Listener {
        void displayDetailsFragment(int stationID);
    }
}
