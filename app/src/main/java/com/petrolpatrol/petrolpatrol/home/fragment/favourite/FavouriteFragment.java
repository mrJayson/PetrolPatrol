package com.petrolpatrol.petrolpatrol.home.fragment.favourite;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.details.DetailsActivity;
import com.petrolpatrol.petrolpatrol.model.Average;
import com.petrolpatrol.petrolpatrol.model.AverageParcel;

import java.util.Map;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGE;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class FavouriteFragment extends Fragment implements FavouritesAdapter.Listener {

    private static final String TAG = makeLogTag(FavouriteFragment.class);

    private Map<String, Average> averages;

    private RecyclerView containerFavouritesListView;
    private FavouritesAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
    public FavouriteFragment() {
        // Required empty public constructor
    }

    public static FavouriteFragment newInstance() {
        return new FavouriteFragment();
    }

    public static FavouriteFragment newInstance(Map<String, Average> averages) {
        FavouriteFragment fragment = new FavouriteFragment();
        Bundle args = new Bundle();
        args.putParcelable(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle savedInstanceState
        if (savedInstanceState != null) {
            AverageParcel averageParcel = savedInstanceState.getParcelable(AverageParcel.ARG_AVERAGE);
            if (averageParcel != null) {
                averages = averageParcel.getAverages();
            }
        }
        // Handle fragment initialisation
        else if (getArguments() != null) {
            AverageParcel averageParcel = getArguments().getParcelable(AverageParcel.ARG_AVERAGE);
            if (averageParcel != null) {
                averages = averageParcel.getAverages();
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        containerFavouritesListView = (RecyclerView) view.findViewById(R.id.container_list_favourites);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        View emptyView = view.findViewById(R.id.empty_view_favourites);
        if (averages == null) {
            // assign the adapter
            adapter = new FavouritesAdapter(getContext(), emptyView, this);
        } else {
            adapter = new FavouritesAdapter(getContext(), averages, emptyView, this);
        }
        containerFavouritesListView.setLayoutManager(layoutManager);
        containerFavouritesListView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.refresh();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (averages != null) {
            outState.putParcelable(AverageParcel.ARG_AVERAGE, new AverageParcel(averages));
        }
        super.onSaveInstanceState(outState);
    }

    public void refresh() {
        getAdapter().refresh();
    }

    public FavouritesAdapter getAdapter() {
        return adapter;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void updateAverages(Map<String, Average> averages) {
        this.averages = averages;
        if (adapter != null) {
            adapter.updateAverages(averages);
        }
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

    @Override
    public void displayDetails(int stationID) {
        DetailsActivity.displayDetails(stationID, averages, getActivity());
    }
}
