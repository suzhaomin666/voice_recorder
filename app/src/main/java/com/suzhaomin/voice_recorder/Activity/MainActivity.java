package com.suzhaomin.voice_recorder.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.suzhaomin.voice_recorder.R.layout.activity_main);
        Log.v("改变","更改的地方");
    }
}
