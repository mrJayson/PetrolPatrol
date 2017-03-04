package com.petrolpatrol.petrolpatrol.details;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.SQLiteClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Price;
import com.petrolpatrol.petrolpatrol.model.Station;

import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class DetailsFragment extends Fragment {

    private static final String TAG = makeLogTag(DetailsFragment.class);

    private static final String ARG_STATION_ID = "stationID";

    private Station station;

    private OnFragmentInteractionListener mListener;

    private RecyclerView containerDetailsListView;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(int stationID) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STATION_ID, stationID);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGI(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SQLiteClient client = new SQLiteClient(getContext());
            client.open();
            station = client.getStation(getArguments().getInt(ARG_STATION_ID));
            client.close();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO set empty adapter here as a placeholder while data is loading
        containerDetailsListView = (RecyclerView) view.findViewById(R.id.container_details_list);
        TextView name = (TextView) view.findViewById(R.id.details_name);
        name.setText(station.getName());
        TextView address = (TextView) view.findViewById(R.id.details_address);
        address.setText(station.getAddress());

        FuelCheckClient client = new FuelCheckClient(getContext());
        client.getFuelPricesForStation(station.getId(), new FuelCheckClient.FuelCheckResponse<List<Price>>() {
            @Override
            public void onCompletion(List<Price> res) {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                RecyclerView.Adapter adapter = new DetailsAdapter(res, getContext());
                containerDetailsListView.setLayoutManager(layoutManager);
                containerDetailsListView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
