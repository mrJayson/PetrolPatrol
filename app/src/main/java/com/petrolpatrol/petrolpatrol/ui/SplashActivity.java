package com.petrolpatrol.petrolpatrol.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.petrolpatrol.petrolpatrol.R;
import com.petrolpatrol.petrolpatrol.datastore.Preferences;
import com.petrolpatrol.petrolpatrol.fuelcheck.FuelCheckClient;
import com.petrolpatrol.petrolpatrol.fuelcheck.VolleyQueue;
import com.petrolpatrol.petrolpatrol.home.HomeActivity;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = makeLogTag(SplashActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Between setContentView and Intent, perform any app initialization during splash screen
        Preferences.getInstance(getApplicationContext()); // Initialize sharedPreferences singleton
        Preferences.getInstance(getApplicationContext()).initialize(); // Populate entries in Preferences if necessary
        VolleyQueue.getInstance(getApplicationContext()); // Initialize volley singleton

        // Perform a reference data check upon start up
        new FuelCheckClient(getApplicationContext()).getReferenceData(new FuelCheckClient.FuelCheckResponse<Object>() {
            @Override
            public void onCompletion(Object res) {
                //TODO App currently hangs if there is no internet, add broadcast receiver for network changes to retry
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish(); // End this activity, don't want the splash screen to linger in the background
            }
        });


    }
}
