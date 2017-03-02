package com.petrolpatrol.petrolpatrol.details;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.text.ParseException;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * Created by jason on 1/03/17.
 */
public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private static final String TAG = makeLogTag(DetailsAdapter.class);


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView price;
        TextView fuelTypeShort;
        TextView fuelTypeLong;
        TextView lastUpdated;

        public ViewHolder(View itemView) {
            super(itemView);

            price = (TextView) itemView.findViewById(R.id.item_details_price);
            fuelTypeShort = (TextView) itemView.findViewById(R.id.item_details_fueltype_short);
            fuelTypeLong = (TextView) itemView.findViewById(R.id.item_details_fueltype_long);
            lastUpdated = (TextView) itemView.findViewById(R.id.item_details_last_updated);
        }
    }

    private List<Price> prices;
    private Context context;

    public DetailsAdapter(List<Price> prices, Context context) {
        this.prices = prices;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details, parent, false);
        return new ViewHolder(v);    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Price price = prices.get(position);

        holder.price.setText(String.valueOf(price.getPrice()));
        holder.fuelTypeShort.setText(price.getFuelType().getCode());
        holder.fuelTypeLong.setText(price.getFuelType().getName());
        holder.lastUpdated.setText(context.getString(R.string.item_details_lastupdated, TimeUtils.timeAgo(price.getLastUpdated())));
    }


    @Override
    public int getItemCount() {
        return prices.size();
    }


}
