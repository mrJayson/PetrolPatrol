package com.petrolpatrol.petrolpatrol.list;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationReceiverFragment;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class ListFragment extends LocationReceiverFragment implements ListAdapter.Listener{

    private static final String TAG = makeLogTag(ListFragment.class);

    private static final String ARG_LIST = "stations";
    private List<Station> stations;
    private Listener parentListener;
    private NewLocationReceiver newLocationReceiver = new NewLocationReceiver(this);


    private SwipeRefreshLayout swipeContainer;
    private RecyclerView containerList;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(List<Station> list) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST, (ArrayList<? extends Parcelable>) list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        LOGI(TAG, "onAttach");

        super.onAttach(context);
        if (context instanceof Listener) {
            parentListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGI(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stations = getArguments().getParcelableArrayList(ARG_LIST);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        containerList = (RecyclerView) view.findViewById(R.id.container_details_list);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.container_swipe_refresh);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGI(TAG, "onRefresh");

                registerReceiverToLocationService();
                parentListener.startLocating();
                ListAdapter adapt = (ListAdapter) containerList.getAdapter();
                adapt.clear();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        LOGI(TAG, "onStart");

        super.onStart();

        getActivity().invalidateOptionsMenu();

        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.Adapter adapter = new ListAdapter(getContext(), stations, parentListener.getSelectedFuelType(), this);
        containerList.setLayoutManager(layoutManager);
        containerList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        LOGI(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LOGI(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LOGI(TAG, "onStop");

        // Unregister if fragment closes while still broadcast receiving
        unregisterReceiverFromLocationService();
        super.onStop();
    }

    @Override
    public void onDetach() {
        LOGI(TAG, "onDetach");

        super.onDetach();
        parentListener = null;
    }

    @Override
    public void onLocationReceived(Location location) {

        LOGI(TAG, "location received");

        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        FuelCheckClient client = new FuelCheckClient(getContext());

        client.getFuelPricesWithinRadius(location.getLatitude(), location.getLongitude(), parentListener.getSelectedSortBy(), parentListener.getSelectedFuelType(), new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {
                parentListener.displayListFragment(res);
            }
        });
    }

    @Override
    public void displayDetailsFragment(int stationID) {
        parentListener.displayDetailsFragment(stationID);
    }

    public interface Listener {
        void startLocating();
        void stopLocating();
        void displayListFragment(List<Station> list);
        void displayDetailsFragment(int stationID);
        String getSelectedFuelType();
        String getSelectedSortBy();
    }
}
