package com.petrolpatrol.petrolpatrol.locate;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.petrolpatrol.petrolpatrol.service.NewLocationReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Listener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements NewLocationReceiver.Listener{

    private static final String TAG = makeLogTag(ListFragment.class);

    private static final String ARG_LIST = "stations";
    private List<Station> stations;
    private Listener parentListener;
    private NewLocationReceiver newLocationReceiver = new NewLocationReceiver(this);


    private SwipeRefreshLayout swipeContainer;
    private RecyclerView container_content;

    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param list The stations to be displayed in the stations fragment.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container_content = (RecyclerView) view.findViewById(R.id.container_content);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.container_swipe_refresh);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGI(TAG, "onRefresh");

                registerReceiverToLocationService();
                parentListener.startLocating();
                ListAdapter adapt = (ListAdapter) container_content.getAdapter();
                adapt.clear();
            }
        });
    }

    /**
     * Get the root view for the fragment's layout (the one returned by {@link #onCreateView}),
     * if provided.
     *
     * @return The fragment's root view, or null if it has no layout.
     */
    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        LOGI(TAG, "onStart");

        super.onStart();
        LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView.Adapter adapter = new ListAdapter(getContext(), stations, parentListener.getSelectedFuelType());
        container_content.setLayoutManager(layoutManager);
        container_content.setAdapter(adapter);
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

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
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

    private void registerReceiverToLocationService() {
        newLocationReceiver.register(getContext());
    }

    private void unregisterReceiverFromLocationService() {
        newLocationReceiver.unregister(getContext());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Listener {
        void startLocating();
        void stopLocating();
        void displayListFragment(List<Station> list);
        String getSelectedFuelType();
        String getSelectedSortBy();
    }
}
