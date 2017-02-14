package com.petrolpatrol.petrolpatrol.locate;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.petrolpatrol.petrolpatrol.R;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocateFragment extends Fragment {

    private static final String TAG = makeLogTag(LocateFragment.class);
    private Listener listener;


    public LocateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_locate, container, false);

        return v;
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
        void getNearbyButtonPressed();
    }

    @Override
    public void onAttach(Context context) {
        // Context is the parent activity
        super.onAttach(context);

        // Check to see if the parent activity has implemented the callback
        if (context instanceof Listener) {
            listener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
