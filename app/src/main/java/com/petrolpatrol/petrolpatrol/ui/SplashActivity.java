package com.petrolpatrol.petrolpatrol.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.petrolpatrol.petrolpatrol.R;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = makeLogTag(SplashActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Between setContentView and Intent, perform any app initialization during splash screen

        Intent intent = new Intent(getApplicationContext(), BaseActivity.class);
        startActivity(intent);
        finish(); // End this activity, don't want the splash screen to linger in the background
    }
}
