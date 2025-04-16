package com.example.financetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financetracker.firebase.FirebaseAuthManager;
import com.example.financetracker.utils.TransactionManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements FirebaseAuthManager.AuthStateCallback {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "FinanceTrackerPrefs";
    private static final String PREF_OFFLINE_MODE = "offline_mode";

    private FirebaseAuthManager authManager;
    private TransactionManager transactionManager;
    private boolean processingAuthChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase managers
        authManager = FirebaseAuthManager.getInstance();
        transactionManager = TransactionManager.getInstance(this);

        // Initialize views
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnContinueOffline = findViewById(R.id.btnContinueOffline);

        // Set up click listeners
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });

        btnContinueOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOfflineMode(true);
                proceedToMainActivity();
            }
        });

        // Check if user is already signed in or has chosen offline mode before registering the listener
        checkPreviousSignIn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register for auth state changes
        authManager.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove auth state listener
        authManager.removeAuthStateListener();
    }

    private void checkPreviousSignIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean offlineMode = prefs.getBoolean(PREF_OFFLINE_MODE, false);

        if (offlineMode) {
            // User has previously chosen offline mode
            setOfflineMode(true);
            proceedToMainActivity();
            return;
        }

        // Check if user is already signed in - let the auth state callback handle it
        // This prevents duplicate handling of the sign-in state
    }

    private void startSignIn() {
        processingAuthChange = true;
        authManager.startSignInFlow(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle sign-in result
        if (requestCode == FirebaseAuthManager.RC_SIGN_IN) {
            boolean success = authManager.handleSignInResult(requestCode, resultCode, data);

            if (!success) {
                // Sign-in failed
                processingAuthChange = false;
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
            // If successful, onUserSignedIn callback will be triggered
        }
    }

    private void setOfflineMode(boolean offlineMode) {
        // Save preference
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_OFFLINE_MODE, offlineMode);
        editor.apply();

        // Configure transaction manager
        transactionManager.setUseFirebase(!offlineMode);

        Log.d(TAG, "App set to " + (offlineMode ? "offline" : "online") + " mode");
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }

    // FirebaseAuthManager.AuthStateCallback implementation
    @Override
    public void onUserSignedIn(FirebaseUser user) {
        Log.d(TAG, "onUserSignedIn: " + user.getUid());
        // Avoid duplicate handling if we're in the middle of sign-in flow
        if (!isFinishing()) {
            setOfflineMode(false);

            // Show success message only if we're coming from a manual sign-in
            if (processingAuthChange) {
                Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
                processingAuthChange = false;
            }

            proceedToMainActivity();
        }
    }

    @Override
    public void onUserSignedOut() {
        Log.d(TAG, "onUserSignedOut");
        // This won't be triggered during normal login flow
        // but is here for completeness
    }
}