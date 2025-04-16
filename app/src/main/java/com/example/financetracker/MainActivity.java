package com.example.financetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.financetracker.fragments.AddTransactionFragment;
import com.example.financetracker.fragments.HomeFragment;
import com.example.financetracker.fragments.ViewReportFragment;
import com.example.financetracker.utils.TransactionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "FinanceTrackerPrefs";

    private TextView tvBudget, tvSpent, tvRemaining;
    private TransactionManager transactionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity started");

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize transaction manager
        transactionManager = TransactionManager.getInstance(this);

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Load the default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Set listener for bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_add_transaction) {
                loadFragment(new AddTransactionFragment());
                return true;
            } else if (id == R.id.nav_reports) {
                loadFragment(new ViewReportFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Launch Settings activity
            startActivity(new android.content.Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Activity Lifecycle Methods with Logging

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: MainActivity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: MainActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: MainActivity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: MainActivity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MainActivity destroyed");
    }
}