package com.example.financetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.financetracker.firebase.FirebaseAuthManager;
import com.example.financetracker.fragments.AddTransactionFragment;
import com.example.financetracker.fragments.HomeFragment;
import com.example.financetracker.fragments.ViewReportFragment;
import com.example.financetracker.utils.TransactionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements FirebaseAuthManager.AuthStateCallback {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "FinanceTrackerPrefs";

    private TextView tvBudget, tvSpent, tvRemaining;
    private TransactionManager transactionManager;
    private FirebaseAuthManager authManager;

    // Menu items that should only be visible when signed in
    private MenuItem syncMenuItem;
    private MenuItem logoutMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity started");

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize managers
        transactionManager = TransactionManager.getInstance(this);
        authManager = FirebaseAuthManager.getInstance();

        // Register for auth state changes
        authManager.addAuthStateListener(this);

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

        // Get references to menu items
        syncMenuItem = menu.findItem(R.id.action_sync);
        logoutMenuItem = menu.findItem(R.id.action_logout);

        // Update menu visibility based on login state
        updateMenuVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Launch Settings activity
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_sync) {
            // Manually sync data with Firebase
            syncWithFirebase();
            return true;
        } else if (id == R.id.action_logout) {
            // Sign out
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void syncWithFirebase() {
        if (!authManager.isUserSignedIn()) {
            Toast.makeText(this, "You must be signed in to sync", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Syncing with cloud...", Toast.LENGTH_SHORT).show();

        // Force a refresh of all transactions
        transactionManager.getAllTransactions();

        Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        try {
            authManager.signOut(this).get();

            // Go back to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error signing out", e);
            Toast.makeText(this, "Error signing out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMenuVisibility() {
        if (syncMenuItem == null || logoutMenuItem == null) return;

        boolean isSignedIn = authManager.isUserSignedIn();
        syncMenuItem.setVisible(isSignedIn);
        logoutMenuItem.setVisible(isSignedIn);
    }

    @Override
    public void onUserSignedIn(FirebaseUser user) {
        Log.d(TAG, "User signed in: " + user.getUid());
        updateMenuVisibility();
    }

    @Override
    public void onUserSignedOut() {
        Log.d(TAG, "User signed out");
        updateMenuVisibility();

        // If user is signed out unexpectedly, go back to login
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove auth state listener
        authManager.removeAuthStateListener();

        Log.d(TAG, "onDestroy: MainActivity destroyed");
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
}