package com.petrolpatrol.petrolpatrol.ui;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.locate.LocateFragment;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity implements LocateFragment.Listener {

    private static final String TAG = makeLogTag(BaseActivity.class);



    private FragmentManager mfragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        displayLocateFragment();
        displayLocateFragment();

    }

    /**
     * Listener for the {@link LocateFragment} fragment
     */

    @Override
    public void getNearbyButtonPressed() {

    }

    private void displayLocateFragment() {
        FragmentTransaction transaction = mfragmentManager.beginTransaction();

        LocateFragment locateFragment = new LocateFragment();

        transaction.replace(R.id.fragment_container, locateFragment);

        transaction.commit();

    }


}
