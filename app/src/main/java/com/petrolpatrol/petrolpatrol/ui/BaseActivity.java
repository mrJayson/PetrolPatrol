package com.petrolpatrol.petrolpatrol.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.petrolpatrol.petrolpatrol.R;

import static com.petrolpatrol.petrolpatrol.util.LogUtils.LOGI;
import static com.petrolpatrol.petrolpatrol.util.LogUtils.makeLogTag;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = makeLogTag(BaseActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOGI(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }
}
