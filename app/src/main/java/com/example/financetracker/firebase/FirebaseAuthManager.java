package com.example.financetracker.firebase;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manager for Firebase Authentication operations
 */
public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    public static final int RC_SIGN_IN = 123;

    // Singleton instance
    private static FirebaseAuthManager instance;

    // Firebase authentication
    private final FirebaseAuth auth;
    private final FirebaseDatabaseManager databaseManager;

    // Auth state listener
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseAuthManager() {
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Get database manager
        databaseManager = FirebaseDatabaseManager.getInstance();
    }

    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    /**
     * Check if a user is currently signed in
     * @return True if a user is signed in, false otherwise
     */
    public boolean isUserSignedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Start the sign-in flow using FirebaseUI Auth
     * @param activity The activity to start the sign-in flow from
     */
    public void startSignInFlow(Activity activity) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false) // Disable SmartLock for development
                .build();

        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle the result from the sign-in flow
     * @param requestCode The request code from onActivityResult
     * @param resultCode The result code from onActivityResult
     * @param data The intent data from onActivityResult
     * @return True if the result was handled, false otherwise
     */
    public boolean handleSignInResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != RC_SIGN_IN) {
            return false;
        }

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = auth.getCurrentUser();
            assert user != null;
            Log.d(TAG, "signInWithEmail:success: " + user.getUid());

            // Update the database references for the new user
            databaseManager.updateUserReference();

            return true;
        } else {
            // Sign in failed
            if (response != null) {
                Log.w(TAG, "signInWithEmail:failure", response.getError());
            } else {
                Log.w(TAG, "signInWithEmail:failure (no response)");
            }
            return false;
        }
    }

    /**
     * Sign out the current user
     * @return A future that completes when the operation is successful
     */
    public CompletableFuture<Void> signOut(Activity activity) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        AuthUI.getInstance()
                .signOut(activity)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signOut:success");
                        future.complete(null);
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    /**
     * Register an authentication state listener
     * @param listener The callback to invoke when the auth state changes
     */
    public void addAuthStateListener(AuthStateCallback listener) {
        if (authStateListener != null) {
            // Remove any existing listener
            auth.removeAuthStateListener(authStateListener);
        }

        // Create and add the new listener
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                listener.onUserSignedIn(user);
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                listener.onUserSignedOut();
            }
        };

        auth.addAuthStateListener(authStateListener);
    }

    /**
     * Remove the authentication state listener
     */
    public void removeAuthStateListener() {
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
            authStateListener = null;
        }
    }

    /**
     * Interface for authentication state callbacks
     */
    public interface AuthStateCallback {
        void onUserSignedIn(FirebaseUser user);
        void onUserSignedOut();
    }
}