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
import android.view.*;

import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.model.Station;
import com.petrolpatrol.petrolpatrol.service.LocationReceiverFragment;
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;
import com.petrolpatrol.petrolpatrol.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class ListFragment extends LocationReceiverFragment implements ListAdapter.Listener{

    private static final String TAG = makeLogTag(ListFragment.class);

    private static final String ARG_LIST = "stationsData";
    private List<Station> stationsData;
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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stationsData = getArguments().getParcelableArrayList(ARG_LIST);
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
        super.onStart();
        Preferences pref = Preferences.getInstance();

        getActivity().invalidateOptionsMenu();

        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView.Adapter adapter = new ListAdapter(getContext(), stationsData, pref.getString(Preferences.Key.SELECTED_FUELTYPE), this);
        containerList.setLayoutManager(layoutManager);
        containerList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        // Unregister if fragment closes while still broadcast receiving
        unregisterReceiverFromLocationService();
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentListener = null;
    }

    @Override
    public void onLocationReceived(Location location) {
        unregisterReceiverFromLocationService();
        parentListener.stopLocating();

        FuelCheckClient client = new FuelCheckClient(getContext());
        Preferences pref = Preferences.getInstance();

        client.getFuelPricesWithinRadius(
                location.getLatitude(),
                location.getLongitude(),
                pref.getString(Preferences.Key.SELECTED_SORTBY),
                pref.getString(Preferences.Key.SELECTED_FUELTYPE),
                new FuelCheckClient.FuelCheckResponse<List<Station>>() {
            @Override
            public void onCompletion(List<Station> res) {
                parentListener.displayListFragment(res);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
        MenuItem menuItem = menu.findItem(R.id.fueltype);
        inflater.inflate(R.menu.submenu_fueltypes, menuItem.getSubMenu());

        Preferences pref = Preferences.getInstance();
        // Preselect the menu_list items recorded in Preferences
        int fuelTypeResID = Utils.identify(pref.getString(Preferences.Key.SELECTED_FUELTYPE), "id", getContext());
        MenuItem fuelType = (MenuItem) menu.findItem(fuelTypeResID);
        fuelType.setChecked(true);

        int sortByResID = Utils.identify("sort_" + pref.getString(Preferences.Key.SELECTED_SORTBY).toLowerCase(), "id", getContext());
        MenuItem sortBy = (MenuItem) menu.findItem(sortByResID);
        sortBy.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.sort_price:
            case R.id.sort_distance:
                item.setChecked(true);
                Preferences.getInstance().put(Preferences.Key.SELECTED_SORTBY, String.valueOf(item.getTitle()));
                return true;
            default:
                try {
                    return Utils.fuelTypeSwitch(id, new Utils.Callback() {
                        @Override
                        public void execute() {
                            item.setChecked(true);
                            Preferences.getInstance().put(Preferences.Key.SELECTED_FUELTYPE, String.valueOf(item.getTitle()));
                        }
                    });
                } catch (NoSuchFieldException e) {
                    return super.onOptionsItemSelected(item);
                }
        }
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
    }
}
