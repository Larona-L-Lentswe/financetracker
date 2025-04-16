package com.example.financetracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "onCreate: Splash screen started");

        // Using Handler to delay the start of MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity after the delay
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // Close this activity
                finish();
                Log.d(TAG, "Splash screen finished, launching MainActivity");
            }
        }, SPLASH_DURATION);
    }

    // Lifecycle methods with logging
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Splash screen visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Splash screen in foreground");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Splash screen paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Splash screen stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Splash screen destroyed");
    }
}