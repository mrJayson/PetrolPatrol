package com.petrolpatrol.petrolpatrol.details;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.util.Colour;
import com.petrolpatrol.petrolpatrol.util.Gradient;
import com.petrolpatrol.petrolpatrol.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private static final String TAG = makeLogTag(DetailsAdapter.class);


    static class ViewHolder extends RecyclerView.ViewHolder {

        View colour;
        TextView price;
        TextView fuelTypeShort;
        TextView fuelTypeLong;
        TextView lastUpdated;

        ViewHolder(View itemView) {
            super(itemView);

            colour = itemView.findViewById(R.id.item_details_colour);
            price = (TextView) itemView.findViewById(R.id.item_details_price);
            fuelTypeShort = (TextView) itemView.findViewById(R.id.item_details_fueltype_short);
            fuelTypeLong = (TextView) itemView.findViewById(R.id.item_details_fueltype_long);
            lastUpdated = (TextView) itemView.findViewById(R.id.item_details_last_updated);
        }
    }

    private List<Price> prices;
    private Map<String, Average> averages;
    private Context context;

    private Gradient gradient;

    DetailsAdapter(Context context, Map<String, Average> averages) {
        this(context, new ArrayList<Price>(), averages);
    }

    DetailsAdapter(Context context, List<Price> prices, Map<String, Average> averages) {
        this.prices = prices;
        this.context = context;
        this.averages = averages;
        this.gradient = new Gradient(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Price price = prices.get(position);

//        gradient.setMeanPrice(averages.get(price.getFuelType().getCode()).getPrice());
//
//        Colour c = gradient.gradiateColour(price.getPrice());
//        holder.colour.setBackgroundColor(c.integer);

        // If the averages are not available, then set the default colour
        if (averages != null && averages.get(price.getFuelType().getCode()) != null) {
            gradient.setMeanPrice(averages.get(price.getFuelType().getCode()).getPrice());
            Colour c = gradient.gradiateColour(price.getPrice());
            holder.colour.setBackgroundColor(c.integer);
        } else {
            holder.colour.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
        }

        holder.price.setText(String.valueOf(price.getPrice()));
        holder.fuelTypeShort.setText(price.getFuelType().getCode());
        holder.fuelTypeLong.setText(price.getFuelType().getName());
        holder.lastUpdated.setText(context.getString(R.string.item_details_lastupdated, TimeUtils.timeAgo(price.getLastUpdated())));
    }

    void updatePrices(List<Price> prices) {
        this.prices = prices;
        notifyDataSetChanged();
    }

    void clear() {
        this.prices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return prices.size();
    }


}
